package com.weekify.common.exception;
// 서버에서 발생한 예외를 클라이언트가 이해할 수 있는 일관된 JSON 형태로 바꾸기 위한 공통 에러 응답 DTO
public record ErrorResponse(
        String code,
        String message
) {
}
