package net.nurigo.kotlinspringdemo

import net.nurigo.sdk.NurigoApp.initialize
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException
import net.nurigo.sdk.message.model.*
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.MultipleDetailMessageSentResponse
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.core.io.ClassPathResource
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


/**
 * 모든 발송 API에는 발신, 수신번호 입력 항목에 +82 또는 +8210, 010-0000-0000 같은 형태로 기입할 수 없습니다.
 * 수/발신 가능한 예시) 01000000000, 020000000 등
 */
@RestController
@RequestMapping("/kakao")
class KakaoExampleController {
    /**
     * 발급받은 API KEY와 API Secret Key를 사용해주세요.
     */
    val messageService: DefaultMessageService =
        initialize("INSERT API KEY", "INSERT API SECRET KEY", "https://api.coolsms.co.kr")

    /**
     * 알림톡 한건 발송 예제
     */
    @PostMapping("/send-one-ata")
    fun sendOneAta(): SingleMessageSentResponse? {
        // 알림톡 템플릿 내에 #{변수} 형태가 존재할 경우 variables를 설정해주세요.
        /*val variables = mutableMapOf<String, String>()
        variables["#{변수명1}"] = "테스트";
        variables["#{변수명2}"] = "치환문구 테스트2";*/

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
        val kakaoOption = KakaoOption(
            pfId = "pfId 입력",
            templateId = "templateId 입력",
            // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
            // disableSms = true,

            // 알림톡 템플릿 내에 #{변수} 형태가 존재할 경우 variables를 설정해주세요.
            // variables = variables,
        )

        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        val message = Message(
            from = "발신번호 입력",
            to = "수신번호 입력",
            kakaoOptions = kakaoOption
        )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        println(response)
        return response
    }

    /**
     * 여러 메시지 발송 예제
     * 한 번 실행으로 최대 10,000건 까지의 메시지가 발송 가능합니다.
     */
    @PostMapping("/send-many-ata")
    fun sendMany(): MultipleDetailMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            val kakaoOption = KakaoOption(
                // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
                pfId = "",
                // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
                templateId = ""
            )
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            val message = Message(
                from = "발신번호 입력",
                to = "수신번호 입력",
                text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i",
                kakaoOptions = kakaoOption
            )
            messageList.add(message)
        }
        try {
            // send 메소드로 단일 Message 객체를 넣어도 동작합니다!
            val response = messageService.send(messageList)

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            // val response = this.messageService.send(messageList, true);
            println(response)
            return response
        } catch (exception: NurigoMessageNotReceivedException) {
            println(exception.failedMessageList)
            println(exception.message)
        } catch (exception: Exception) {
            println(exception.message)
        }
        return null
    }

    /**
     * 예약 발송 예제(단건 및 여러 건 발송을 지원합니다)
     */
    @PostMapping("/send-ata-scheduled-messages")
    fun sendScheduledMessages(): MultipleDetailMessageSentResponse? {
        val messageList = ArrayList<Message>()
        for (i in 0..2) {
            val kakaoOption = KakaoOption(
                // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
                pfId = "",
                // 등록하신 카카오 알림톡 템플릿의 templateId를 입력해주세요.
                templateId = ""
            )
            // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
            val message = Message(
                from = "발신번호 입력",
                to = "수신번호 입력",
                text = "한글 45자, 영자 90자 이하 입력되면 자동으로 SMS타입의 메시지가 추가됩니다.$i",
                kakaoOptions = kakaoOption
            )
            messageList.add(message)
        }
        try {
            // 과거 시간으로 예약 발송을 진행할 경우 즉시 발송처리 됩니다.
            val localDateTime: LocalDateTime =
                LocalDateTime.parse("2022-05-27 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            val zoneOffset: ZoneOffset = ZoneId.systemDefault().rules.getOffset(localDateTime)
            val instant: Instant = localDateTime.toInstant(zoneOffset)

            // 단일 발송도 지원하여 ArrayList<Message> 객체가 아닌 Message 단일 객체만 넣어도 동작합니다!
            val response: MultipleDetailMessageSentResponse = messageService.send(messageList, instant)

            // 중복 수신번호를 허용하고 싶으실 경우 위 코드 대신 아래코드로 대체해 사용해보세요!
            // val response = this.messageService.send(messageList, instant, true);
            println(response)
            return response
        } catch (exception: NurigoMessageNotReceivedException) {
            println(exception.failedMessageList)
            println(exception.message)
        } catch (exception: Exception) {
            println(exception.message)
        }
        return null
    }

    /**
     * 친구톡 한건 발송 예제, send many 호환
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cta")
    fun sendOneCta(): SingleMessageSentResponse? {
        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        val kakaoOption = KakaoOption(
            pfId = "pfId 입력",
            variables = null,
            // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
            // disableSms = true,
        )

        // 친구톡에 버튼을 넣으실 경우에만 추가해주세요.
        val kakaoButtons = ArrayList<KakaoButton>()
        // 웹링크 버튼
        val kakaoWebLinkButton = KakaoButton(
            "테스트 버튼1", KakaoButtonType.WL,
            "https://example.com", "https://example.com",
            null, null
        )

        // 앱링크 버튼
        val kakaoAppLinkButton = KakaoButton(
            "테스트 버튼2", KakaoButtonType.AL,
            null, null,
            "exampleapp://test", "exampleapp://test"
        )

        // 봇 키워드 버튼, 버튼을 클릭하면 버튼 이름으로 수신자가 발신자에게 채팅을 보냅니다.
        val kakaoBotKeywordButton = KakaoButton(
            "테스트 버튼3", KakaoButtonType.BK, null, null, null, null
        )

        // 메시지 전달 버튼, 버튼을 클릭하면 버튼 이름과 친구톡 메시지 내용을 포함하여 수신자가 발신자에게 채팅을 보냅니다.
        val kakaoMessageDeliveringButton = KakaoButton(
            "테스트 버튼4", KakaoButtonType.MD, null, null, null, null
        )

        /*
         * 상담톡 전환 버튼, 상담톡 서비스를 이용하고 있을 경우 상담톡으로 전환. 상담톡 서비스 미이용시 해당 버튼 추가될 경우 발송 오류 처리됨.
         * @see <a href="https://business.kakao.com/info/bizmessage/">상담톡 딜러사 확인</a>
         */
        /*KakaoButton kakaoBotCustomerButton = new KakaoButton(
                "테스트 버튼6", KakaoButtonType.BC, null, null, null, null
        );*/

        // 봇전환 버튼, 해당 비즈니스 채널에 카카오 챗봇이 없는 경우 동작안함.
        // KakaoButton kakaoBotTransferButton = new KakaoButton("테스트 버튼7", KakaoButtonType.BT, null, null, null, null);
        kakaoButtons.add(kakaoWebLinkButton)
        kakaoButtons.add(kakaoAppLinkButton)
        kakaoButtons.add(kakaoBotKeywordButton)
        kakaoButtons.add(kakaoMessageDeliveringButton)
        kakaoOption.buttons = kakaoButtons

        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        val message = Message(
            from = "발신번호 입력",
            to = "수신번호 입력",
            text = "친구톡 테스트 메시지",
            kakaoOptions = kakaoOption
        )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        println(response)
        return response
    }

    /**
     * 친구톡 이미지 단건 발송, send many 호환
     * 친구톡 내 버튼은 최대 5개까지만 생성 가능합니다.
     */
    @PostMapping("/send-cti")
    @Throws(IOException::class)
    fun sendOneCti(): SingleMessageSentResponse? {
        val resource = ClassPathResource("static/cti.jpg")
        val file = resource.file
        // 이미지 크기는 가로 500px 세로 250px 이상이어야 합니다, 링크도 필수로 기입해주세요.
        val imageId = messageService.uploadFile(file, StorageType.KAKAO, "https://example.com")

        // 등록하신 카카오 비즈니스 채널의 pfId를 입력해주세요.
        val kakaoOption = KakaoOption(
            pfId = "pfId 입력",
            imageId = imageId,
            variables = null,
            // disableSms를 true로 설정하실 경우 문자로 대체발송 되지 않습니다.
            // disableSms = true,
        )

        // 발신번호 및 수신번호는 반드시 01012345678 형태로 입력되어야 합니다.
        val message = Message(
            from = "발신번호 입력",
            to = "수신번호 입력",
            text = "친구톡 테스트 메시지",
            kakaoOptions = kakaoOption
        )
        val response = messageService.sendOne(SingleMessageSendingRequest(message))
        println(response)
        return response
    }
}