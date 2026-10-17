package se.mobiledev.lab;

import static org.junit.Assert.fail;

import org.junit.Test;

public class BranchProtectionProbeTest {
    @Test
    public void failsOnPurpose() {
        fail("Intentional failure to verify admin branch protection");
    }
}
