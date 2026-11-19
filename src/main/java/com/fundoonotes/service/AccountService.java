package com.fundoonotes.service;

import com.fundoonotes.dto.request.ResetPasswordRequestDto;
import com.fundoonotes.entity.User;

public interface AccountService {

    void sendRegistrationVerification(User user);

    void verifyEmail(String token);

    void resendVerification(String email);

    void forgotPassword(String email);

    void resetPassword(ResetPasswordRequestDto request);
}
