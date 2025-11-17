package com.adobe.livecycle.watermark;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class BitTest 
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
    public void bitTest()
    {

        try {
            int wmBitsLength = 10;
            String text = generateRandBits(wmBitsLength);
            System.out.println(text);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static String generateRandBits(int numBits) {
/* 317 */     Math.rint(0.0D);
/* 318 */     String randBits = "";
/*     */     
/* 320 */     int i = 0;
/* 321 */     while (i++ < numBits) {
/* 322 */       double d = Math.random();
/* 323 */       long val = Math.round(d);
/*     */       
/* 325 */       randBits = randBits.concat(Long.toString(val));
/*     */     } 
/* 327 */     return randBits;
/*     */   }


}
