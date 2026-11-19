package com.fundoonotes.aop;

import org.aspectj.lang.JoinPoint;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;

@Aspect
@Component
public class NoteActivityAspect {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NoteActivityAspect.class
            );

    // UC13: Monitor successful note-changing operations.
    // Keep the pointcut specific to NoteServiceImpl.
    @Pointcut(
            "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.createNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.updateNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.deleteNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.pinNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.unpinNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.archiveNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.unarchiveNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.moveToTrash(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.restoreNote(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.setReminder(..)) || " +

                    "execution(* com.fundoonotes.service.impl." +
                    "NoteServiceImpl.removeReminder(..))"
    )
    public void noteModification() {
    }

    // Runs when a selected method returns normally.
    @AfterReturning("noteModification()")
    public void logSuccessfulModification(
            JoinPoint joinPoint
    ) {

        String methodName =
                joinPoint.getSignature().getName();

        // Never log note titles, descriptions, JWTs
        // or raw method arguments.
        log.info(
                "UC13 ACTIVITY: Note operation completed: {}",
                methodName
        );
    }
}
