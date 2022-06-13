package net.nurigo.gradlespringdemo;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.StorageType;
import net.nurigo.sdk.message.request.MessageListRequest;
import net.nurigo.sdk.message.request.MultipleMessageSendingRequest;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MessageListResponse;
import net.nurigo.sdk.message.response.MultipleDetailMessageSentResponse;
import net.nurigo.sdk.message.response.MultipleMessageSentResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@RestController
public class ExampleController {

    final DefaultMessageService messageService;

    public ExampleController() {
        this.messageService = NurigoApp.INSTANCE.initialize("INSERT API KEY", "INSERT API SECRET KEY", "https://api.coolsms.co.kr");
    }

    /**
     * 메시지 조회 예제
     */
    @GetMapping("/get-message-list")
    public void getMessageList() {
        MessageListResponse response = this.messageService.getMessageList(new MessageListRequest());

        System.out.println(response);
    }

    /**
     * 단일 메시지 발송 예제
     */
    @PostMapping("/send-one")
    public SingleMessageSentResponse sendOne() {
        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.");

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return response;
    }

    /**
     * MMS 발송 예제
     * 단일 발송, 여러 건 발송 상관없이 이용 가능
     */
    @PostMapping("/send-mms")
    public SingleMessageSentResponse sendMmsByResourcePath() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/sample.jpg");
        File file = resource.getFile();
        String imageId = this.messageService.uploadFile(file, StorageType.MMS, null);

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.");
        message.setImageId(imageId);

        // 여러 건 메시지 발송일 경우 send many 예제와 동일하게 구성하여 발송할 수 있습니다.
        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return response;
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many")
    public MultipleDetailMessageSentResponse sendMany() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);

            messageList.add(message);
        }

        try {
            // send 메소드로 단일 Message 객체를 넣어도 동작합니다!
            MultipleDetailMessageSentResponse response = this.messageService.send(messageList);

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            //MultipleDetailMessageSentResponse response = this.messageService.send(messageList, true);

            System.out.println(response);

            return response;
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }


    @PostMapping("/send-scheduled-messages")
    public MultipleDetailMessageSentResponse sendScheduledMessages() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);

            messageList.add(message);
        }

        try {
            // 과거 시간으로 예약 발송을 진행할 경우 즉시 발송처리 됩니다.
            LocalDateTime localDateTime = LocalDateTime.parse("2022-05-27 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(localDateTime);
            Instant instant = localDateTime.toInstant(zoneOffset);

            // 단일 발송도 지원하여 ArrayList<Message> 객체가 아닌 Message 단일 객체만 넣어도 동작합니다!
            MultipleDetailMessageSentResponse response = this.messageService.send(messageList, instant);

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            //MultipleDetailMessageSentResponse response = this.messageService.send(messageList, instant, true);

            System.out.println(response);

            return response;
        } catch (NurigoMessageNotReceivedException exception) {
            System.out.println(exception.getFailedMessageList());
            System.out.println(exception.getMessage());
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }
        return null;
    }

    /**
     * 잔액 조회 예제
     */
    @GetMapping("/get-balance")
    public Balance getBalance() {
        Balance balance = this.messageService.getBalance();
        System.out.println(balance);

        return balance;
    }
}
