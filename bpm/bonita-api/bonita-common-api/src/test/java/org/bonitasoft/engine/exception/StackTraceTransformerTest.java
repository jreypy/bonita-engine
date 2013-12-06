package org.bonitasoft.engine.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bonitasoft.engine.api.internal.ServerWrappedException;
import org.bonitasoft.engine.bpm.process.ProcessInstanceNotFoundException;
import org.junit.Test;

public class StackTraceTransformerTest {

    @Test
    public void merge_stack_keep_only_current_exception() throws Exception {
        ServerWrappedException e = new ServerWrappedException(new IllegalStateException(new NoClassDefFoundError("org.Unknown")));

        ServerWrappedException newE = StackTraceTransformer.mergeStackTraces(e);

        assertNull(newE.getCause().getCause());
    }

    @Test
    public void merge_stack_keep_stack_of_cause() throws Exception {
        ProcessInstanceNotFoundException cause3 = new ProcessInstanceNotFoundException("the process");
        BonitaRuntimeException cause2 = new BonitaRuntimeException("org.Unknown", cause3);
        IllegalStateException cause = new IllegalStateException(cause2);
        ServerWrappedException e = new ServerWrappedException(cause);

        ServerWrappedException newE = StackTraceTransformer.mergeStackTraces(e);
        assertTrue(containsStack(newE.getCause().getStackTrace(), "BonitaRuntimeException"));
        assertTrue(containsStack(newE.getCause().getStackTrace(), "ProcessInstanceNotFoundException"));
    }

    @Test
    public void merge_stack_reduce_stack_length() throws Exception {
        ProcessInstanceNotFoundException cause3 = new ProcessInstanceNotFoundException("the process");
        BonitaRuntimeException cause2 = new BonitaRuntimeException("org.Unknown", cause3);
        IllegalStateException cause = new IllegalStateException(cause2);
        ServerWrappedException e = new ServerWrappedException(cause);
        cause.printStackTrace();
        ServerWrappedException newE = StackTraceTransformer.mergeStackTraces(e);
        newE.getCause().printStackTrace();
        noDuplicate(newE.getCause());
    }

    private void noDuplicate(final Throwable throwable) throws Exception {
        HashSet<StackTraceElement> hashSet = new HashSet<StackTraceElement>();
        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
            if (!hashSet.add(stackTraceElement) && stackTraceElement.toString().contains("org.bonitasoft")) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "the stacktrace contains 2 times " + stackTraceElement, throwable);
                throw new Exception("the stacktrace contains 2 times " + stackTraceElement, throwable);
            }
        }

    }

    private boolean containsStack(final StackTraceElement[] stackTrace, final String string) {
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().contains(string)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void merge_stack_keep_message_of_cause() throws Exception {
        ProcessInstanceNotFoundException cause3 = new ProcessInstanceNotFoundException("the process");
        BonitaRuntimeException cause2 = new BonitaRuntimeException("org.Unknown", cause3);
        IllegalStateException cause = new IllegalStateException(cause2);
        ServerWrappedException e = new ServerWrappedException(cause);

        ServerWrappedException newE = StackTraceTransformer.mergeStackTraces(e);

        assertTrue(containsMessage(newE.getCause().getStackTrace(), "org.Unknown"));
        assertTrue(containsMessage(newE.getCause().getStackTrace(), "the process"));
    }

    @Test
    public void merge_stack_when_no_cause() throws Exception {
        ProcessInstanceNotFoundException cause = new ProcessInstanceNotFoundException("the process");
        ServerWrappedException e = new ServerWrappedException(cause);

        ServerWrappedException newE = StackTraceTransformer.mergeStackTraces(e);

        assertEquals("the process", cause.getMessage());
        assertEquals(cause, newE.getCause());
    }

    @Test
    public void merge_stack_work_even_with_security_restriction() throws Exception {
        ProcessInstanceNotFoundException cause3 = new ProcessInstanceNotFoundException("the process");
        BonitaRuntimeException cause2 = new BonitaRuntimeException("org.Unknown", cause3);
        IllegalStateException cause = new IllegalStateException(cause2);
        ServerWrappedException e = new ServerWrappedException(cause);
        Field field = StackTraceTransformer.field;
        StackTraceTransformer.field = null;
        ServerWrappedException newE = StackTraceTransformer.mergeStackTraces(e);
        StackTraceTransformer.field = field;

        assertTrue(containsMessage(newE.getCause().getStackTrace(), "org.Unknown"));
        assertTrue(containsMessage(newE.getCause().getStackTrace(), "the process"));
    }

    private boolean containsMessage(final StackTraceElement[] stackTrace, final String string) {
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getMethodName().contains(string)) {
                return true;
            }
        }
        return false;
    }

}
