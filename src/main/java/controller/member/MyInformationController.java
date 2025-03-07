package controller.member;

import domain.member.Member;
import domain.member.MemberSchedule;
import domain.trainer.Trainer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import repository.ReservationRepository;
import repository.TrainerRepository;
import service.SmsService;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.ResourceBundle;

import static domain.member.SelectedMember.loginMember;
import static util.MemberUtil.*;
import static util.PageUtil.movePage;

public class MyInformationController implements Initializable {
    private final TrainerRepository trainerRepository = new TrainerRepository();
    private final ReservationRepository reservationRepository = new ReservationRepository();
    private final SmsService smsService = new SmsService();

    @FXML
    private Label memberName, trainerName, gymTicketRemain, PTTicketRemain, lockerNo, lockerRemain,
            clothesAvailability, clothesRemain, trainerPhone;
    @FXML
    private HBox myPtInformation;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (loginMember != null) {
            Member member = loginMember;

            try {
                setMyInfo(member);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //내 정보 페이지의 정보를 바꿔주는 메소드
    @FXML
    public void setMyInfo(Member member) throws ParseException {
        int memberNum = member.getNum();
        int trainerNum = getTrainerNumForMember(memberNum);
        List<Integer> remain = getRemainAll(memberNum);
        List<MemberSchedule> memberSchedules;
        LocalDate today = LocalDate.now();
        int gymTicket = remain.get(0);
        int PTTicket = remain.get(1);
        int clothes = remain.get(2);
        int locker = remain.get(3);

        memberSchedules = reservationRepository.findMemberSchedule(memberNum);
        int reservationNum = memberSchedules.size();

        memberName.setText(member.getName() + " ");

        if(trainerNum == 0) {
            trainerName.setText("");
            trainerPhone.setText("담당 트레이너가 없습니다");

        }else{
            Trainer trainer = trainerRepository.findByNum(trainerNum);

            String phone = trainer.getPhone();
            String calculatedTrainerPhone = "010 - " + phone.substring(0, 4) + " - " + phone.substring(4, 8);

            trainerName.setText(trainer.getName() + " 트레이너");
            trainerPhone.setText(calculatedTrainerPhone);
        }

        if(reservationNum > 0) {
            myPtInformation.getChildren().clear();
            for(int i = 0; i < reservationNum; i++) {
                int startTime = memberSchedules.get(i).getReservationTime();
                String reservationTime;
                if(startTime < 9){
                    StringBuilder sb = new StringBuilder();
                    sb.append("0").append(startTime).append(":00 ~ 0").append(startTime + 1).append(":00");
                    reservationTime = sb.toString();
                }else if(startTime == 9){
                    StringBuilder sb = new StringBuilder();
                    sb.append("0").append(startTime).append(":00 ~ ").append(startTime + 1).append(":00");
                    reservationTime = sb.toString();
                }else{
                    StringBuilder sb = new StringBuilder();
                    sb.append(startTime).append(":00 ~ ").append(startTime + 1).append(":00");
                    reservationTime = sb.toString();
                }

                VBox newVBox = new VBox();
                newVBox.setSpacing(5);
                newVBox.setAlignment(Pos.CENTER);
                newVBox.getStyleClass().add("myInformation_PTReservation_One");

                Label newLabel = new Label("예약 " + (i + 1));
                newLabel.getStyleClass().add("myInformation_PTReservation_Num");
                newVBox.getChildren().add(newLabel);

                String reservationDate = memberSchedules.get(i).getReservationDate().toString().replace("-", "/");
                newLabel = new Label(reservationDate);
                newLabel.getStyleClass().add("myInformation_PTReservation_Date");
                newVBox.getChildren().add(newLabel);

                newLabel = new Label(reservationTime);
                newLabel.getStyleClass().add("myInformation_PTReservation_Time");
                newVBox.getChildren().add(newLabel);

                myPtInformation.getChildren().add(newVBox);
            }
        }

        if(gymTicket == 0) {
            gymTicketRemain.setText("결제 내역이 없습니다");
        }else {
            LocalDate expireDate = today.plusDays(gymTicket);
            long daysUntilExpire = ChronoUnit.DAYS.between(today, expireDate);
            String expireDateText = expireDate.toString().replace("-", "/");
            gymTicketRemain.setText(expireDateText + " (D - " + daysUntilExpire + ")");
        }

        PTTicketRemain.setText(PTTicket + "개");

        if(locker == 0){
            lockerNo.setText("현재 이용 중인 사물함 없음");
            lockerRemain.setText("");
        }else{
            int lockerNum = getLockerNum(memberNum);

            LocalDate expireDate = today.plusDays(locker);
            lockerNo.setText("No." + lockerNum);
            String expireDateText = expireDate.toString().replace("-", "/");

            lockerRemain.setText(expireDateText + " (D - " + locker + ")");
        }

        if(clothes == 0){
            clothesAvailability.setText("현재 이용 중인 운동복 없음");
            clothesRemain.setText("");
        }else{
            LocalDate expireDate = today.plusDays(clothes);
            clothesAvailability.setText("이용 가능");
            String expireDateText = expireDate.toString().replace("-", "/");

            clothesRemain.setText(expireDateText + " (D - " + clothes + ")");

        }
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        movePage(event, "/view/member/helloMember");
    }

    @FXML
    public void callAdmin(){
        smsService.callAdmin();
    }
}