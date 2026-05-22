package com.weekify.common.exception;

// FieldErrorResponse는 유효성 검증에 실패한 필드명과 해당 필드의 에러 메시지를 담는 공통 응답 DTO이다.

public record FieldErrorResponse(
        String field,
        String message
) {
}
