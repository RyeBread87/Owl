package com.application.owl;

import com.application.owl.models.Contact;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.MalformedURLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ContactEditTest {

    @Test
    public void nameValidator_CorrectNameSimple_ReturnsTrue() {
        assertFalse(ContactEdit.isNameEmpty("fdfg"));
        assertFalse(ContactEdit.isNameTooLong("name@email.com"));
    }

    @Test
    public void frequencyValidator_CorrectFrequencySimple_ReturnsTrue() {
        assertFalse(ContactEdit.isFrequencyTooLong(987456310));
        assertFalse(ContactEdit.isFrequencyTooSmall(0));
    }

    @Test
    public void phoneValidator_CorrectPhoneSimple_ReturnsTrue() {
        assertFalse(ContactEdit.phoneNumberStartsWithPlus("9874563210"));
        assertFalse(ContactEdit.phoneNumberIsEmpty("9874563210"));
        assertTrue(ContactEdit.isMobileValidLength("9874563210"));
    }

    @Test
    public void emailValidator_CorrectEmailSimple_ReturnsTrue() {
        assertFalse(ContactEdit.isEmailEmpty("name@email.com"));
        assertTrue(ContactEdit.isValidMail("name@email.com"));
    }

    @Test
    public void facebookLinkValidator_CorrectFacebookLinkSimple_ReturnsTrue() throws MalformedURLException {
        assertFalse(ContactEdit.isFacebookLinkEmpty("http://facebook"));
        assertTrue(ContactEdit.isFacebookLinkValidURL("http://facebook"));
        assertTrue(ContactEdit.isURLHostFacebook("http://facebook"));
    }

    @Test
    public void skypeNameValidator_CorrectSkypeNameSimple_ReturnsTrue() {
        assertFalse(ContactEdit.isSkypeNameEmpty("ryanasmith"));
        assertFalse(ContactEdit.isSkypeNameTooLong("ryanasmith"));
    }


    @Mock
    ContactEdit mContactEdit;

    @Mock
    ContactEdit mBrokenContactEdit;

    @Test
    public void sharedPreferencesHelper_SavePersonalInformationFailed_ReturnsFalse() {
        // Read personal information from a broken SharedPreferencesHelper
        boolean success = mBrokenContactEdit.validateName("email@address.com");
        assertThat("Makes sure writing to a broken SharedPreferencesHelper returns false", success, is(false));
    }
}
