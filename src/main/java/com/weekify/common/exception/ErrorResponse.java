package com.weekify.common.exception;

import java.util.List;

// 서버에서 발생한 예외를 클라이언트가 이해할 수 있는 일관된 JSON 형태로 바꾸기 위한 공통 에러 응답 DTO
public record ErrorResponse(
        String code,
        String message,
        List<FieldErrorResponse> errors
) {
    // from(ErrorCode) : ErrorCode에 정의된 기본 메시지 그대로 사용
    public static ErrorResponse from(ErrorCode errorCode){
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                List.of()
        );
    }

    // of(ErrorCode, message) : ErrorCode의 code는 유지하고, message만 지정
    public static ErrorResponse of(ErrorCode errorCode, String message){
        return new ErrorResponse(
                errorCode.getCode(),
                message,
                List.of()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldErrorResponse> errors){
        return new ErrorResponse(
                errorCode.getCode(),
                errorCode.getMessage(),
                errors
        );
    }
}
