package com.example.tradetrackeruser.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    // 루트 디렉터리 하위의 클래스 파일들의 모든 메서드에 적용
    @Pointcut("execution(* com.example.tradetrackeruser..*.*(..))")
    private void cut(){}

    /*
    // PointCut에 의해 필터링 된 경로로 들어오는 경우 메서드 호출 전에 적용
    @Before("cut()")
    public void beforeLog(JoinPoint joinPoint) {
        Method method = getMethod(joinPoint);
        log.info("==== method name: {} ====", method.getName());

        // 파라미터 받아오기
        Object[] args = joinPoint.getArgs();
        if (args.length <= 0) log.info("no parameter");

        for (Object arg: args) {
            log.info("parameter type: {}", arg.getClass().getSimpleName());
            log.info("parameter value: {}", arg);
        }
    }

    // PointCut에 의해 필터링 된 경로로 등러오는 경우 메서드 리턴 후에 적용
    @AfterReturning(value = "cut()", returning = "returnObj")
    public void afterLog(JoinPoint joinPoint, Object returnObj) {
        Method method = getMethod(joinPoint);
        log.info("==== method name: {} ====", method.getName());

        log.info("return type: {}", returnObj.getClass().getSimpleName());
        log.info("return value: {}", returnObj);
    }
     */

    /**
     * @Around 어노테이션은 실제 메서드 호출 전후에 적용됨
     * @param proceedingJoinPoint: JoinPoint의 서브 인터페이스로 proceed() 메서드를 통해 메서드의 실행을 제어할 수 있음
     * @return
     * @throws Throwable
     */
    @Around(value = "cut()")
    public Object aroundLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Method method = getMethod(proceedingJoinPoint);
        // 파라미터 받아오기
        Object[] args = proceedingJoinPoint.getArgs();

        // 요청 로그
        logRequest(method, args);

        Object returnObj = null;
        try {
            // proceed() 메서드로 실제 메서드 실행
            returnObj = proceedingJoinPoint.proceed();
            // 응답 로그
            logResponse(method, returnObj);
        } catch (Throwable throwable) {
            // 에러 로그
            logError(method, throwable);
            throw throwable;    // 예외를 다시 던져서 호출자에게 전달
        }

        return returnObj;
    }

    private void logRequest(Method method, Object[] args) {
        log.info("==== Request method: {} ====", method.getName());

        if (args.length == 0) {
            log.info("no parameter");
        } else {
            for (Object arg: args) {
                log.info("Parameter type: {}", arg.getClass().getSimpleName());
                log.info("Parameter value: {}", arg);
            }
        }
    }

    private void logResponse(Method method, Object returnObj) {
        log.info("==== Response method: {} ====", method.getName());

        if (returnObj != null) {
            log.info("Return type: {}", returnObj.getClass().getSimpleName());
            log.info("Return value: {}\n", returnObj);
        } else {
            log.info("Return value: null\n");
        }
    }

    private void logError(Method method, Throwable throwable) {
        String errorMessage = throwable.getMessage();
        log.error("==== Error method: {} ====", method.getName());
        log.error("Error message: {}", errorMessage);
    }

    // JoinPoint로 메서드 정보 가져오기
    private Method getMethod(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getMethod();
    }
}
