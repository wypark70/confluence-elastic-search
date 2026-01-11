package com.atsoft.confluence.plugin.elasticsearch.action;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.xwork.HttpMethod;
import com.atlassian.xwork.PermittedMethods;

/**
 * Confluence Action 예제
 * ConfluenceActionSupport를 상속받아 웹 페이지를 렌더링
 */
public class DialogAction extends ConfluenceActionSupport {

    // 다이얼로그에 전달할 데이터
    private String dialogTitle;
    private String message;

    public DialogAction() {
        // 기본 생성자
    }

    /**
     * 기본 execute 메서드
     * SUCCESS를 반환하면 dialog.vm 템플릿이 렌더링됨
     */
    @Override
    @PermittedMethods({HttpMethod.GET})
    public String execute() throws Exception {
        // 데이터 준비
        this.dialogTitle = "다이얼로그 제목";
        this.message = "ConfluenceActionSupport를 사용한 예제입니다.";

        return SUCCESS; // "success" 반환 -> dialog.vm 렌더링
    }

    // Getter 메서드 (Velocity 템플릿에서 사용)
    public String getDialogTitle() {
        return dialogTitle;
    }

    public String getMessage() {
        return message;
    }


}
