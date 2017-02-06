package com.tcs.innovations.omnia.arc;


/**
 * Created by irvel on 29/09/2015.
 */
public class Emoji {
    private float mLeftEyeOpen;
    private float mRightEyeOpen;
    private float mSmiling;
    private String mEmojiChar;

    public static final float UNCOMPUTED_PROBABILITY = -1.0F;

    public Emoji(){
        mLeftEyeOpen = UNCOMPUTED_PROBABILITY;
        mRightEyeOpen = UNCOMPUTED_PROBABILITY;
        mSmiling = UNCOMPUTED_PROBABILITY;
        mEmojiChar = "";
    }

    public String getEmojiChar() {
        return mEmojiChar;
    }

    public void setExpression(float leftEyeOpen, float rightEyeOpen, float smiling){
        mLeftEyeOpen = leftEyeOpen;
        mRightEyeOpen = rightEyeOpen;
        mSmiling = smiling;
        calculateEmoji();
    }

    //==============================================================================================
    // Calculate Emoji
    //==============================================================================================

    /**
     * Calculates the corresponding emoji based on the probabilities of the open left eye,
     * open right eye and smile. The emoji values are currently hard-coded because linking
     * them a string resource triggers a force-close.
     * TODO: Implement a more robust emoji-face model
     */

    private void calculateEmoji() {
        //If there is no smile, display a sad emoji
        if(mSmiling == UNCOMPUTED_PROBABILITY){
            if(mLeftEyeOpen > .8 && mRightEyeOpen > .8) {
                //Worried Face
                mEmojiChar = "\uD83D\uDE1F";
            }
            else if((mLeftEyeOpen > .3 && mLeftEyeOpen < .8) && (mRightEyeOpen > .3 && mRightEyeOpen < .8) ) {
                //Astonished Face
                mEmojiChar = "\uD83D\uDE32";
            }
            else{
                //Disappointed Face
                mEmojiChar = "\uD83D\uDE1E";
            }
        }
        else{
            if(mLeftEyeOpen > .8 && mRightEyeOpen > .8) {
                if(mSmiling > .7) {
                    //Smiling Face With Open Mouth and Smiling Eyes
                    mEmojiChar = "\uD83D\uDE04";
                }
                else{
                    //Grinning Face With Smiling Eyes
                    mEmojiChar = "\uD83D\uDE01";
                }
            }
            else if((mLeftEyeOpen > .3 && mLeftEyeOpen < .8) && (mRightEyeOpen > .3 && mRightEyeOpen < .8) ) {
                //Grinning Face
                mEmojiChar = "\uD83D\uDE00";
            }
            else if(mLeftEyeOpen < .5 && mRightEyeOpen > .5 || mLeftEyeOpen > .5 && mRightEyeOpen < .5 ) {
                //Winking Face
                mEmojiChar = "\uD83D\uDE09";
            }
            else{
                if(mSmiling > .45){
                    //Smiling Face With Open Mouth and Tightly-Closed Eyes
                    mEmojiChar = "\uD83D\uDE06";
                }
                else{
                    //Expressionless Face
                    mEmojiChar = "\uD83D\uDE11";
                }
            }
        }
    }
}

