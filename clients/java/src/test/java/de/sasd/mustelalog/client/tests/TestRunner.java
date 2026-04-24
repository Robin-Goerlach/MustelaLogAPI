package de.sasd.mustelalog.client.tests;

import java.util.ArrayList;
import java.util.List;

public final class TestRunner {
    private TestRunner() {
    }

    public static void main(String[] args) throws Exception {
        List<TestCase> tests = List.of(
                new TestCase("JsonSelfTest", JsonSelfTest::run),
                new TestCase("TimeServiceSelfTest", TimeServiceSelfTest::run),
                new TestCase("ExportSelfTest", ExportSelfTest::run),
                new TestCase("SettingsLoaderSelfTest", SettingsLoaderSelfTest::run)
        );
        ArrayList<String> failures = new ArrayList<>();
        for (TestCase test : tests) {
            try {
                test.runnable().run();
                System.out.println("PASS: " + test.name());
            } catch (Throwable throwable) {
                String message = "FAIL: " + test.name() + " -> " + throwable.getMessage();
                failures.add(message);
                throwable.printStackTrace(System.err);
            }
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("Self-tests failed: " + failures);
        }
        System.out.println("All self-tests passed.");
    }

    private record TestCase(String name, RunnableWithException runnable) {}

    @FunctionalInterface
    public interface RunnableWithException {
        void run() throws Exception;
    }
}
