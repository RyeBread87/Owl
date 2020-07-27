package com.application.owl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class MessageEditTest {

    @Test
    public void messageBodyValidator_CorrectMessageBodySimple_ReturnsTrue() {
        assertFalse(MessageEdit.isMessageBodyEmpty("name@email.com"));
        assertTrue(MessageEdit.isMessageBodyTooLong("name@email.com"));
    }
}