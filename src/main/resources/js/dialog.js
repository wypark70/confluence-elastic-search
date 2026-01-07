(function ($) {
    // AJS 초기화
    AJS.toInit(function () {


        // 1. [다이얼로그 열기] 버튼 클릭
        $("body").on("click", "#btn-open-my-dialog", function (e) {
            e.preventDefault();

            // 1. 이미 다이얼로그가 로드되어 있는지 확인
            if ($("#my-custom-dialog").length > 0) {
                // 이미 있으면 바로 보여주고 끝
                $("#feedback-text").val(""); // 내용 초기화
                AJS.dialog2("#my-custom-dialog").show();
                return;
            }

            // 2. 없으면 서버에서 HTML 가져오기 (최초 1회 실행)
            var $btn = $(this);
            $btn.attr("disabled", true); // 로딩 중 중복 클릭 방지

            $.ajax({
                url: AJS.contextPath() + "/rest/myplugin/1.0/dialog/view", // 위에서 만든 API
                type: "GET",
                dataType: "html", // HTML을 받아옴
                success: function (htmlData) {
                    // 3. 받아온 HTML을 body 끝에 추가
                    $("body").append(htmlData);

                    // 4. 다이얼로그 표시
                    AJS.dialog2("#my-custom-dialog").show();
                },
                error: function (xhr) {
                    AJS.flag({
                        type: 'error',
                        title: '로딩 실패',
                        body: '다이얼로그를 불러오지 못했습니다.'
                    });
                },
                complete: function() {
                    $btn.attr("disabled", false);
                }
            });
        });

        // 2. [취소] 버튼 클릭
        $("body").on("click", "#btn-close-dialog", function (e) {
            e.preventDefault();
            AJS.dialog2("#my-custom-dialog").hide();
        });

        // 3. [전송] 버튼 클릭 (REST API 호출)
        $("body").on("click", "#btn-submit-dialog", function (e) {
            e.preventDefault();

            var content = $("#feedback-text").val();
            if (!content) {
                // AUI 스타일 경고 메시지
                AJS.flag({
                    type: 'warning',
                    title: '입력 오류',
                    body: '내용을 입력해주세요.',
                    close: 'auto'
                });
                return;
            }

            // 로딩 표시 및 버튼 비활성화
            var $btn = $(this);
            $btn.attr("disabled", true);
            $("#dialog-spinner").show();

            // REST API 호출
            $.ajax({
                url: AJS.contextPath() + "/rest/myplugin/1.0/feedback",
                type: "POST",
                contentType: "application/json",
                data: JSON.stringify({ "message": content }),
                success: function (response) {
                    // 성공 시 닫기
                    AJS.dialog2("#my-custom-dialog").hide();

                    // 성공 알림
                    AJS.flag({
                        type: 'success',
                        title: '전송 성공',
                        body: '서버에 데이터가 저장되었습니다.',
                        close: 'auto'
                    });
                },
                error: function (xhr, status, error) {
                    // 에러 알림
                    AJS.flag({
                        type: 'error',
                        title: '전송 실패',
                        body: '에러 코드: ' + xhr.status + ' ' + error
                    });
                },
                complete: function () {
                    // UI 상태 복구
                    $btn.attr("disabled", false);
                    $("#dialog-spinner").hide();
                }
            });
        });

    });

})(AJS.$);