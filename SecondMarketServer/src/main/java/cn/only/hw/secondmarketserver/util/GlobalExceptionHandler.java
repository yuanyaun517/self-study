package cn.only.hw.secondmarketserver.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSqlIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL integrity constraint violation", ex);
        String message = ex.getMessage();
        if (message != null && message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            if (split.length >= 3) {
                return Result.error("\u8d26\u53f7" + split[2] + "\u5df2\u5b58\u5728");
            }
            return Result.error("\u8d26\u53f7\u5df2\u5b58\u5728");
        }
        return Result.error("\u6570\u636e\u5199\u5165\u5931\u8d25");
    }

    @ExceptionHandler(DataAccessException.class)
    public Result<String> handleDataAccessException(DataAccessException ex) {
        log.error("Database access failed", ex);
        return Result.error("\u6570\u636e\u5e93\u64cd\u4f5c\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5 user \u8868\u7ed3\u6784\u540e\u91cd\u8bd5");
    }

    @ExceptionHandler(CustomException.class)
    public Result<String> handleCustomException(CustomException ex) {
        log.error("Custom exception", ex);
        return Result.error("\u64cd\u4f5c\u5931\u8d25\uff1a" + ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("Upload file too large", ex);
        return Result.error("\u4e0a\u4f20\u6587\u4ef6\u8fc7\u5927\uff0c\u8bf7\u538b\u7f29\u540e\u91cd\u8bd5\uff08\u5355\u6587\u4ef6\u4e0d\u8d85\u8fc710MB\uff09");
    }

    @ExceptionHandler(MultipartException.class)
    public Result<String> handleMultipartException(MultipartException ex) {
        log.error("Multipart request failed", ex);
        return Result.error("\u6587\u4ef6\u4e0a\u4f20\u5931\u8d25\uff0c\u8bf7\u68c0\u67e5\u6587\u4ef6\u5927\u5c0f\u548c\u683c\u5f0f\u540e\u91cd\u8bd5");
    }
}
