package net.nurigo.springdemo;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.model.StorageType;
import net.nurigo.sdk.message.request.MessageListRequest;
import net.nurigo.sdk.message.request.MultipleMessageSendingRequest;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MessageListResponse;
import net.nurigo.sdk.message.response.MultipleMessageSentResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 모든 발송 API에는 발신, 수신번호 입력 항목에 +82 또는 +8210, 010-0000-0000 같은 형태로 기입할 수 없습니다.
 * 수/발신 가능한 예시) 01000000000, 020000000 등
 */
@RestController
public class ExampleController {

    private final DefaultMessageService messageService;

    /**
     * 발급받은 API KEY와 API Secret Key를 사용해주세요.
     */
    public ExampleController() {
        this.messageService = NurigoApp.INSTANCE.initialize("", "", "https://api.coolsms.co.kr");
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
        message.setFrom("029302266");
        message.setTo("01000000000");
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
        message.setFrom("029302266");
        message.setTo("01000000000");
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
    public MultipleMessageSentResponse sendMany() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Message message = new Message();
            message.setFrom("029302266");
            message.setTo("01000000000");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);

            messageList.add(message);
        }
        MultipleMessageSendingRequest request = new MultipleMessageSendingRequest(messageList);
        // allowDuplicates를 true로 설정하실 경우 중복으로 수신번호를 입력해도 각각 발송됩니다.
        // request.setAllowDuplicates(true);

        MultipleMessageSentResponse response = this.messageService.sendMany(request);
        System.out.println(response);

        return response;
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
