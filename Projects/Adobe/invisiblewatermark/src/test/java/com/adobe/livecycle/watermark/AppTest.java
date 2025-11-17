package com.adobe.livecycle.watermark;

//import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
//    @Test
 //   public void shouldAnswerWithTrue()
  //  {
   //     assertTrue( true );
  //  }

    @SuppressWarnings("static-access")
    @Test
    public void watermarkTest()
    {
        InvisibleWatermarkTool wmTool = new InvisibleWatermarkTool();

        try {
            wmTool.encodeInvisibleWatermark("/Users/joyner/Documents/Projects/Adobe/invisiblewatermark/resource/TestFile.pdf", "AltonJoyner", "/Users/joyner/Documents/Projects/Adobe/invisiblewatermark/resource/wmTestFile.pdf");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
