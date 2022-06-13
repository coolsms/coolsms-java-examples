package net.nurigo.gradlespringdemo;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.model.*;
import net.nurigo.sdk.message.request.MultipleMessageSendingRequest;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.MultipleDetailMessageSentResponse;
import net.nurigo.sdk.message.response.MultipleMessageSentResponse;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * 모든 발송 API에는 발신, 수신번호 입력 항목에 +82 또는 +8210, 010-0000-0000 같은 형태로 기입할 수 없습니다.
 * 수/발신 가능한 예시) 01000000000, 020000000 등
 */
@RestController
@RequestMapping("/kakao")
public class KakaoExampleController {

    private final DefaultMessageService messageService;

    /**
     * 발급받은 API KEY와 API Secret Key를 사용해주세요.
     */
    public KakaoExampleController() {
        this.messageService = NurigoApp.INSTANCE.initialize("INSERT API KEY", "INSERT API SECRET KEY", "https://api.coolsms.co.kr");
    }

    /**
     * 알림톡 한건 발송 예제
     */
    @PostMapping("/send-one-ata")
    public SingleMessageSentResponse sendOneAta() {
        KakaoOption kakaoOption = new KakaoOption();
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.setDisableSms(true);

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.setPfId("");
        // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
        kakaoOption.setTemplateId("");

        // 알림톡 템플릿 내에 #{변수} 형태가 존재할 경우 variables를 설정해주세요.
        /*
        HashMap<String, String> variables = new HashMap<>();
        variables.put("#{변수명1}", "테스트");
        variables.put("#{변수명2}", "치환문구 테스트2");
        kakaoOption.setVariables(variables);
        */

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setKakaoOptions(kakaoOption);

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return response;
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many-ata")
    public MultipleDetailMessageSentResponse sendMany() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            KakaoOption kakaoOption = new KakaoOption();
            // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
            kakaoOption.setPfId("");
            // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
            kakaoOption.setTemplateId("");

            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);
            message.setKakaoOptions(kakaoOption);

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

    @PostMapping("/send-ata-scheduled-messages")
    public MultipleDetailMessageSentResponse sendScheduledMessages() {
        ArrayList<Message> messageList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            KakaoOption kakaoOption = new KakaoOption();
            // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
            kakaoOption.setPfId("");
            // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
            kakaoOption.setTemplateId("");

            Message message = new Message();
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            message.setFrom("발신번호 입력");
            message.setTo("수신번호 입력");
            message.setText("한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다." + i);
            message.setKakaoOptions(kakaoOption);

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
     * 친구톡 한건 발송 예제, send 호환, 다량 발송의 경우 위 send-many-ata 코드를 참조해보세요!
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cta")
    public SingleMessageSentResponse sendOneCta() {
        KakaoOption kakaoOption = new KakaoOption();
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.setDisableSms(true);

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.setPfId("");
        kakaoOption.setVariables(null);

        // 친구톡에 버튼을 넣으실 경우에만 추가해주세요.
        ArrayList<KakaoButton> kakaoButtons = new ArrayList<>();
        // 웹링크 버튼
        KakaoButton kakaoWebLinkButton = new KakaoButton(
                "테스트 버튼1", KakaoButtonType.WL,
                "https://example.com", "https://example.com",
                null, null
        );

        // 앱링크 버튼
        KakaoButton kakaoAppLinkButton = new KakaoButton(
                "테스트 버튼2", KakaoButtonType.AL,
                null, null,
                "exampleapp://test", "exampleapp://test"
        );

        // 봇 키워드 버튼, 버튼을 클릭하면 버튼 이름으로 수신자가 발신자에게 채팅을 보냅니다.
        KakaoButton kakaoBotKeywordButton = new KakaoButton(
                "테스트 버튼3", KakaoButtonType.BK, null, null, null, null
        );

        // 메시지 전달 버튼, 버튼을 클릭하면 버튼 이름과 친구톡 메시지 내용을 포함하여 수신자가 발신자에게 채팅을 보냅니다.
        KakaoButton kakaoMessageDeliveringButton = new KakaoButton(
                "테스트 버튼4", KakaoButtonType.MD, null, null, null, null
        );

        /*
         * 상담톡 전환 버튼, 상담톡 서비스를 이용하고 있을 경우 상담톡으로 전환. 상담톡 서비스 미이용시 해당 버튼 추가될 경우 발송 오류 처리됨.
         * @see <a href="https://business.kakao.com/info/bizmessage/">상담톡 딜러사 확인</a>
         */
        /*KakaoButton kakaoBotCustomerButton = new KakaoButton(
                "테스트 버튼6", KakaoButtonType.BC, null, null, null, null
        );*/

        // 봇전환 버튼, 해당 비즈니스 채널에 카카오 챗봇이 없는 경우 동작안함.
        // KakaoButton kakaoBotTransferButton = new KakaoButton("테스트 버튼7", KakaoButtonType.BT, null, null, null, null);

        kakaoButtons.add(kakaoWebLinkButton);
        kakaoButtons.add(kakaoAppLinkButton);
        kakaoButtons.add(kakaoBotKeywordButton);
        kakaoButtons.add(kakaoMessageDeliveringButton);

        kakaoOption.setButtons(kakaoButtons);

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setText("친구톡 테스트 메시지");
        message.setKakaoOptions(kakaoOption);

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return response;
    }

    /**
     * 친구톡 이미지 단건 발송, send 호환, 다량 발송의 경우 위 send-many-ata 코드를 참조해보세요!
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cti")
    public SingleMessageSentResponse sendOneCti() throws IOException {
        ClassPathResource resource = new ClassPathResource("static/cti.jpg");
        File file = resource.getFile();
        // 이미지 크기는 가로 500px 세로 250px 이상이어야 합니다, 링크도 필수로 기입해주세요.
        String imageId = this.messageService.uploadFile(file, StorageType.KAKAO, "https://example.com");

        KakaoOption kakaoOption = new KakaoOption();
        // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
        // kakaoOption.setDisableSms(true);

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        kakaoOption.setPfId("");
        kakaoOption.setImageId(imageId);
        kakaoOption.setVariables(null);

        Message message = new Message();
        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        message.setFrom("발신번호 입력");
        message.setTo("수신번호 입력");
        message.setText("테스트");
        message.setKakaoOptions(kakaoOption);

        SingleMessageSentResponse response = this.messageService.sendOne(new SingleMessageSendingRequest(message));
        System.out.println(response);

        return response;
    }
}
