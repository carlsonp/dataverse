/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 *
 * @author skraffmi
 */
public class TermsOfUseAndAccessValidator implements ConstraintValidator<ValidateTermsOfUseAndAccess, TermsOfUseAndAccess> {

    @Override
    public void initialize(ValidateTermsOfUseAndAccess constraintAnnotation) {

    }

    @Override
    public boolean isValid(TermsOfUseAndAccess value, ConstraintValidatorContext context) {
        //If there are no restricted files then terms are valid  
        if (!value.getDatasetVersion().isHasRestrictedFile()) {
            return true;
        }

        /*If there are restricted files then the version
        must allow access requests or have terms of access filled in.
         */
        boolean valid = value.isFileAccessRequest() == true || (value.getTermsOfAccess() != null && !value.getTermsOfAccess().isEmpty());
        if (!valid) {
            try {
                if (context != null) {
                    context.buildConstraintViolationWithTemplate("If Request Access is false then Terms of Access must be provided.").addConstraintViolation();
                }

                String message = "Constraint violation found in Terms of Use and Access. "
                        + " If Request Access to restricted files is set to false then Terms of Access must be provided.";
                value.setValidationMessage(message);
            } catch (NullPointerException e) {
                return false;
            }
            return false;
        }

        return valid;
    }

}
