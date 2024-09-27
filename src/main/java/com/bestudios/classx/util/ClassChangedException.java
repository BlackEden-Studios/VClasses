package com.bestudios.classx.util;

import com.bestudios.classx.Classes;

public class ClassChangedException extends Exception{

    private final Classes newClass;

    private final Classes previousClass;

    public ClassChangedException(Classes newClassType, Classes previousClassType) {
        super();
        newClass = newClassType;
        previousClass = previousClassType;
    }

    public Classes getNewClass() {
        return newClass;
    }

    public Classes getPreviousClass() {
        return previousClass;
    }
}
