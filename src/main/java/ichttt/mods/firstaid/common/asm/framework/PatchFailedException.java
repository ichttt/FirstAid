package ichttt.mods.firstaid.common.asm.framework;

public class PatchFailedException extends RuntimeException {

    private static String format(String className, String message) {
        return "There was a severe error while patching the class " + className + " and loading cannot continue.\n" +
                "The reason is most likely that the source file is different from what we expect.\n" +
                "This may be because of other coremods or an invalid minecraft installation.\n" +
                "Further details: " + message;
    }

    public PatchFailedException(String className, String message) {
        super(format(className, message));
    }

    public PatchFailedException(String className, String message, Throwable cause) {
        super(format(className, message), cause);
    }
}
