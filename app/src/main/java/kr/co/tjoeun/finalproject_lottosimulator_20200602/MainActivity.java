package kr.co.tjoeun.finalproject_lottosimulator_20200602;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kr.co.tjoeun.finalproject_lottosimulator_20200602.databinding.ActivityMainBinding;

public class MainActivity extends BaseActivity {

    ActivityMainBinding binding;

    int[] winLottoNumArr = new int[6];
    int bonusNum = 0;

    List<TextView> winNumTxts = new ArrayList<>();

    long useMoney = 0L;

    long winMoney = 0L;

    int firstRankCount = 0;
    int secondRankCount = 0;
    int thirdRankCount = 0;
    int fourthRankCount = 0;
    int fifthRankCount = 0;
    int unRankedCount = 0;

    List<TextView> myNumTxts = new ArrayList<>();

    Boolean isAutoBuyRunning = false;

//    같은 핸들러를 여러곳에서 사용하니까 => 멤버변수로 생성 (공유해서 여러곳에서 사용하려고)
    Handler mHandler = new Handler();

//    구매로직 코드도 여러곳에서 사용하려고 멤버변수로 생성성
   Runnable buyLottoRunnable = new Runnable() {
        @Override
        public void run() {

//            사용한 금액이 1천만원 이하라면
            if(useMoney<10000000){
//                로또번호 생성 + 등수 맞추기 진행
                makeLottoWinNumbers();
                checkWinRank();

//                이행동을 다시 할일로 등록해달라 => 반복으로 동작하게 되는 이유
                mHandler.post(buyLottoRunnable);
            }
            else{
//                돈을 다썼으면 로또 구매 종료 => 추가로 할일을 등록하지않는다
                Toast.makeText(mContext,"로또구매를 종료합니다.",Toast.LENGTH_SHORT).show();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main);
        setupEvents();
        setValues();

    }

    @Override
    public void setupEvents() {

//        자동구매를 눌렀을때
        binding.buyAutoLottoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                지금 구매를 안돌리고 있다면
                if(!isAutoBuyRunning){
//                    구매시작코드를 할일로 등록 => mHandler가 실행
                    mHandler.post(buyLottoRunnable);
//                    구매가 돌아가고있따고 명시
                    isAutoBuyRunning=true;
//                    버튼의 문구도 중단하기로 변경
                    binding.buyAutoLottoBtn.setText(getResources().getString(R.string.pause_auto_buying));
                }
//                지금 구매를 돌리고 있다면
                else{
//                    예정된 다음 구매 행동을 할일에서 제거
//                    더이상 할일이 없으니 정지된다
                    mHandler.removeCallbacks(buyLottoRunnable);
//                    구매를 하지않고 있다고 명시
                    isAutoBuyRunning=false;
//                    다시누르면 재개한다고 알려준다
//                    문구를 res/values/string.xml에서 가져오는 코드
                    binding.buyAutoLottoBtn.setText(getResources().getString(R.string.resume_auto_buying));
                }

            }
        });

//        한장을 구매할때 => 로또번호 만들고 등수 확인만 실행
        binding.buyOneLottoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                makeLottoWinNumbers();
                checkWinRank();

            }
        });

    }

    @Override
    public void setValues() {

//        당첨번호 텍스트뷰들을 ArrayList에 넣어둠
//        당첨번호를 적어둘떄 편리하게 짜려고
        winNumTxts.add(binding.winNumTxt01);
        winNumTxts.add(binding.winNumTxt02);
        winNumTxts.add(binding.winNumTxt03);
        winNumTxts.add(binding.winNumTxt04);
        winNumTxts.add(binding.winNumTxt05);
        winNumTxts.add(binding.winNumTxt06);

//        내 입력번호도 같은 처리 진행
        myNumTxts.add(binding.myNumTxt01);
        myNumTxts.add(binding.myNumTxt02);
        myNumTxts.add(binding.myNumTxt03);
        myNumTxts.add(binding.myNumTxt04);
        myNumTxts.add(binding.myNumTxt05);
        myNumTxts.add(binding.myNumTxt06);

    }

//    등수확인코드
    void checkWinRank(){

//        사용 금액은 무조건 1000원증가
        useMoney+=1000;

//        증가된 사용금액을 화면에 반영(중복코드)
        binding.useMoneyTxt.setText(String.format("%,d원",useMoney));

//        맞춘갯수를 저장하는 변수
        int correctCount = 0;

//        내 입력번호가 적힌 텍스트뷰들(setValues 참고)을 꺼내봄
        for(TextView myNumTxt:myNumTxts){

//            적혀있는 숫자(String)를 int로 변경
            int myNum = Integer.parseInt(myNumTxt.getText().toString());

//            내 숫자를 들고 => 당첨번호를 돌면서 확인
            for(int winNum:winLottoNumArr){

//                같은 걸 찾았다면 맞춘갯수 1개 증가
                if(myNum==winNum){
                    correctCount++;
                }

            }

        }

//        맞춘갯수에 따른 등수 판정 + 당첨금액 누적
        if(correctCount==6){
            winMoney+=1300000000;
            firstRankCount++;
        }
        else if(correctCount==5){

//            5개를 맞췄으때 보너스 번호 여부에 따라 2등/3등이 갈림
            boolean isBonusNumCorrect = false;

//            내 입력 번호 텍스트뷰 목록을 돌면서 확인
            for(TextView myNumTxt:myNumTxts){

//                텍스트뷰에 적힌 내용을 int로 변경
                int myNum = Integer.parseInt(myNumTxt.getText().toString());

//                보너스 번호와 비교해서 같은게 있다면 보너스를 맞췄다고 처리
//                한번도 이 분기에 못들어왔따 => 보너스 못맞췄따
                if(myNum==bonusNum){
                    isBonusNumCorrect=true;
                    break;
                }

            }

//            보너스 맞추면 2등 아니면 3등
            if(isBonusNumCorrect){
                winMoney+=54000000;
                secondRankCount++;
            }
            else {
                winMoney+=1450000;
                thirdRankCount++;
            }

        }
        else if(correctCount==4){
            winMoney+=50000;
            fourthRankCount++;
        }
        else if(correctCount==3){
            winMoney+=5000;
            fifthRankCount++;
        }
        else{
//            3개도 못맞췄다면 전부 낙점 처리
            unRankedCount++;
        }

//        사용금액과 당첨금액 화면에 표시
        binding.winMoneyTxt.setText(String.format("%,d원",winMoney));
        binding.useMoneyTxt.setText(String.format("%,d원",useMoney));

//        각등수별(낙첨포함) 횟수를 화면에 표시
        binding.firstRankTxt.setText(String.format("%d회",firstRankCount));
        binding.secondRankTxt.setText(String.format("%d회",secondRankCount));
        binding.thirdRankTxt.setText(String.format("%d회",thirdRankCount));
        binding.fourthRankTxt.setText(String.format("%d회",fourthRankCount));
        binding.fifthRankTxt.setText(String.format("%d회",fifthRankCount));
        binding.unrankedTxt.setText(String.format("%d회",unRankedCount));

    }

//    로또 번호 만들기 코드
    void makeLottoWinNumbers(){
//        지난주 당첨번호가 새 당첨번호에 영향 주는 것을 막기위한 조치
//        기존 ㅏㄷㅇ첨번호를 모두 0으로 세팅
        for(int i=0;i<winLottoNumArr.length;i++){

            winLottoNumArr[i]=0;

        }
//        보너스 번호도 0으로 세팅
        bonusNum=0;

//        당처번호 6개를 뽑기위한 for문
        for(int i=0;i<winLottoNumArr.length;i++){

//            조건에 맞는(중복이 아닌) 숫자를 뽑을때까지 무한반복
            while(true){

//                1~45중 숫자를 랜덤
                int randomNum = (int) (Math.random()*45+1);

//                중복검사 결과를 저장하는 변수 => 일단 맞다고해놓고 틀리면 false로 변경
                boolean isDuplicatedOk = true;

//                당첨된 번호중 같은게 있다면 false로 변경
//                한번도 같은게 없었다면 true 유지
                for(int num:winLottoNumArr){
                    if(num==randomNum){
                        isDuplicatedOk=false;
                        break;
                    }
                }

//                중복검사가 통과되었다면
                if(isDuplicatedOk){
//                    당첨번호로 등록
                    winLottoNumArr[i] = randomNum;
//                    무한반복 탈출 => 다음 당첨번호 뽑으러감 (for의 다음 i)
                    break;
                }
            }

        }

//        6개를 뽑는 for문이 다 돌고 나면 => 순서가 뒤죽박죽
//        Arrays 클래스의 static 메쏘드를 사용해서 오름차순으로 정렬
        Arrays.sort(winLottoNumArr);

//        보너스번호를 뽑는 무한 반복 => 숫자 하나만 뽑는다 => for문대신 while문사용
        while (true){

//            1~45사이의 랜덤한 숫자(앞과같음)
            int randomNum = (int) (Math.random()*45+1);

            boolean isDuplicatedOk = true;

            for(int num:winLottoNumArr){

                if(num==randomNum){
                    isDuplicatedOk=false;
                    break;
                }

            }

            if(isDuplicatedOk){
                bonusNum=randomNum;
                break;
            }

        }

//        당첨 번호들을 텍스트뷰에 표시
        for(int i=0;i<winNumTxts.size();i++){

            int winNum = winLottoNumArr[i];

//            화면에 있는 당첨번호 텍스트뷰들을 ArrayList에 담아두고 (setValues참고) 활용
            winNumTxts.get(i).setText(winNum+"");
        }

//        보너스번호도 화면에 표시
        binding.bonusNumTxt.setText(bonusNum+"");

    }












}

