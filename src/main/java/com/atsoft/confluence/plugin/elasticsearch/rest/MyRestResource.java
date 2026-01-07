package com.atsoft.confluence.plugin.elasticsearch.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

// XML에 설정한 path와 일치해야 함
@Path("/feedback")
public class MyRestResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveFeedback(FeedbackModel data) {

        // 실제 비즈니스 로직 (예: DB 저장, 이메일 발송 등)
        System.out.println("User Feedback Received: " + data.getMessage());

        return Response.ok("{\"status\":\"success\"}").build();
    }

    // JSON 매핑용 내부 모델 클래스
    @XmlRootElement
    public static class FeedbackModel {
        @XmlElement
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}