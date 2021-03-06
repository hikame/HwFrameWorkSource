package android.test;

import android.view.View;
import android.view.ViewGroup;
import junit.framework.Assert;

@Deprecated
public class ViewAsserts {
    private ViewAsserts() {
    }

    public static void assertOnScreen(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        boolean z = true;
        int y = xy[1] - xyRoot[1];
        Assert.assertTrue("view should have positive y coordinate on screen", y >= 0);
        if (y > view.getRootView().getHeight()) {
            z = false;
        }
        Assert.assertTrue("view should have y location on screen less than drawing height of root view", z);
    }

    public static void assertOffScreenBelow(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        boolean z = true;
        int y = xy[1] - xyRoot[1];
        String str = "view should have y location on screen greater than drawing height of origen view (" + y + " is not greater than " + origin.getHeight() + ")";
        if (y <= origin.getHeight()) {
            z = false;
        }
        Assert.assertTrue(str, z);
    }

    public static void assertOffScreenAbove(View origin, View view) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        boolean z = true;
        if (xy[1] - xyRoot[1] >= 0) {
            z = false;
        }
        Assert.assertTrue("view should have y location less than that of origin view", z);
    }

    public static void assertHasScreenCoordinates(View origin, View view, int x, int y) {
        int[] xy = new int[2];
        view.getLocationOnScreen(xy);
        int[] xyRoot = new int[2];
        origin.getLocationOnScreen(xyRoot);
        Assert.assertEquals("x coordinate", x, xy[0] - xyRoot[0]);
        Assert.assertEquals("y coordinate", y, xy[1] - xyRoot[1]);
    }

    public static void assertBaselineAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not baseline aligned", xy[1] + first.getBaseline(), xy[1] + second.getBaseline());
    }

    public static void assertRightAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not right aligned", xy[0] + first.getMeasuredWidth(), xy[0] + second.getMeasuredWidth());
    }

    public static void assertRightAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not right aligned", Math.abs((xy[0] + first.getMeasuredWidth()) - (xy[0] + second.getMeasuredWidth())), margin);
    }

    public static void assertLeftAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstLeft = xy[0];
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not left aligned", firstLeft, xy[0]);
    }

    public static void assertLeftAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstLeft = xy[0];
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not left aligned", Math.abs(firstLeft - xy[0]), margin);
    }

    public static void assertBottomAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not bottom aligned", xy[1] + first.getMeasuredHeight(), xy[1] + second.getMeasuredHeight());
    }

    public static void assertBottomAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not bottom aligned", Math.abs((xy[1] + first.getMeasuredHeight()) - (xy[1] + second.getMeasuredHeight())), margin);
    }

    public static void assertTopAligned(View first, View second) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstTop = xy[1];
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not top aligned", firstTop, xy[1]);
    }

    public static void assertTopAligned(View first, View second, int margin) {
        int[] xy = new int[2];
        first.getLocationOnScreen(xy);
        int firstTop = xy[1];
        second.getLocationOnScreen(xy);
        Assert.assertEquals("views are not top aligned", Math.abs(firstTop - xy[1]), margin);
    }

    public static void assertHorizontalCenterAligned(View reference, View test) {
        int[] xy = new int[2];
        reference.getLocationOnScreen(xy);
        int referenceLeft = xy[0];
        test.getLocationOnScreen(xy);
        Assert.assertEquals("views are not horizontally center aligned", (reference.getMeasuredWidth() - test.getMeasuredWidth()) / 2, xy[0] - referenceLeft);
    }

    public static void assertVerticalCenterAligned(View reference, View test) {
        int[] xy = new int[2];
        reference.getLocationOnScreen(xy);
        int referenceTop = xy[1];
        test.getLocationOnScreen(xy);
        Assert.assertEquals("views are not vertically center aligned", (reference.getMeasuredHeight() - test.getMeasuredHeight()) / 2, xy[1] - referenceTop);
    }

    public static void assertGroupIntegrity(ViewGroup parent) {
        int count = parent.getChildCount();
        Assert.assertTrue("child count should be >= 0", count >= 0);
        for (int i = 0; i < count; i++) {
            Assert.assertNotNull("group should not contain null children", parent.getChildAt(i));
            Assert.assertSame(parent, parent.getChildAt(i).getParent());
        }
    }

    public static void assertGroupContains(ViewGroup parent, View child) {
        int count = parent.getChildCount();
        Assert.assertTrue("Child count should be >= 0", count >= 0);
        boolean found = false;
        for (int i = 0; i < count; i++) {
            if (parent.getChildAt(i) == child) {
                if (!found) {
                    found = true;
                } else {
                    Assert.assertTrue("child " + child + " is duplicated in parent", false);
                }
            }
        }
        Assert.assertTrue("group does not contain " + child, found);
    }

    public static void assertGroupNotContains(ViewGroup parent, View child) {
        int count = parent.getChildCount();
        Assert.assertTrue("Child count should be >= 0", count >= 0);
        for (int i = 0; i < count; i++) {
            if (parent.getChildAt(i) == child) {
                Assert.assertTrue("child " + child + " is found in parent", false);
            }
        }
    }
}
