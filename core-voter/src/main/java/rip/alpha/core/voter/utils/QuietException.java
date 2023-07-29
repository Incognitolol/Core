package rip.alpha.core.voter.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class QuietException extends Exception {
    public QuietException(String s) {
        super(s);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}