package com.projectwhitekey;
/**
*This Class acts as a kind of container for each sound file. In the current build of the project, it simply contains the pitch of the note.
*Under the current naming convention, it does not do much; however, in the future, we plan to include different instruments which may
*force us to change our naming convention for our files. This will allow us to simplify that process. 
*/
public class Note
{
    public enum noteName
    {
        A, Bb, B, C, Db, D, Eb, E, F, Gb, G, Ab, Error
    }
 
    //attributes
    private String name;    //the name of the note as defined in the enum
    private int pitch;        //pitch value
    private int soundFile;    //fileName of the associated sound to be played
       
    public Note(int name)
    {
        this.pitch = name;
    }
       

	//GETTER METHODS
    public String getSoundFile()                        { return String.valueOf(soundFile); }
    public int getPitch()                                               { return pitch; }
    public String getNoteName()                                 { return name.toString(); }
       
    //SETTER METHODS
    public void setSoundFile(int soundFile)     { this.soundFile = soundFile; }
    public void setPitch(int newPitch)          { this.pitch = newPitch; }
    public void setNoteName(String newName)   { this.name = newName; }
 
    public static noteName pitchToNoteName(int p)
    {
        //bring down the pitch until it is within the range of 1-11, in the event that we need the name of the given note if we only have a pitch
        while(p>=12) p-=12;
        switch (p)
        {
            case 0:  return noteName.A;
            case 1:  return noteName.Bb;
            case 2:  return noteName.B;
            case 3:  return noteName.C;
            case 4:  return noteName.Db;
            case 5:  return noteName.D;
            case 6:  return noteName.Eb;
            case 7:  return noteName.E;
            case 8:  return noteName.F;
            case 9:  return noteName.Gb;
            case 10: return noteName.G;
            case 11: return noteName.Ab;
        }
        return noteName.Error;
    }
}