package com.projectwhitekey;
 
/**
 * The idea behind this class is that it will supply the keyboard with notes to use corresponding to the scale desired.
 * This class is, essentially, a glorified chooser. You supply it with notes, and some parameters, and it gives you the
 * relevant notes based on those parameters. You should feed in the entire spectrum of Note objects you will need to operate
 * the instrument this Scale object gets tied to.
 * The keyboard should have one Scale object associated with it in order to get the notes it needs to play.
 * @author Mitch & Andrew, error handling by Jace
 *
 */
public class Scale
{
        //Defines the available scale types.
        public enum scaleType
        {
                pentatonic_major,              
                pentatonic_minor,              
                major,
                minor_natural,
                minor_harmonic,
                minor_melodic,
                gypsy_minor,
                gypsy_major
        }
       
        //Declarations
        private Note[] ret;                     //The array of notes that will be mapped to the keys
        private Note[] everyNote;               //This is the array of notes to pick from right from the start.
        private Note root;                      //The first note in this particular scale.
        private scaleType currentScale;         //The name of the scale.
       
        /**
         * Feed the constructor an array of every note you will need, the root of the scale (for example, if you point to a piano and
         * ask, "If I want to build a major scale from this note, what would I need?" the root is the note you were pointing to,) scale
         * is the name of the scale you want to build, and notes is the array of notes you are choosing from. So, the notes you feed
         * into this class contain the soundfiles relevant to the instrument you presumably attached the scale to.
         * @param root The root of the scale (i.e, the first element)
         * @param scale The type of scale you want to build
         * @param notes Every single note you want to choose from
         */
        public Scale(Note root, Scale.scaleType scale, Note[] notes)
        {
                this.root = root;
                this.everyNote = notes;
                this.currentScale = scale;
        }
 
        //Getters
        public scaleType getScaleType() { return currentScale; }
        public Note getRoot()                   { return root; }
        public Note[] getNotes()                { return everyNote; }
       
        //Setters
        public void setRoot(Note root)                  { this.root = root; }
        public void setScaleType(scaleType st)  { this.currentScale = st; }
       
        /**
         * This is pretty much the meat of the class. You ask for a number of elements to generate, and it gives it to you
         * based on the Scale's root and scaleType. For example, if
         * @param elements
         * @return
         */
        public Note[] getScaleElements(int elements)
        {
                ret = new Note[elements];
                ret[0] = root;  //Since we always start with the root, this will always be true
                int startingPitch = root.getPitch();
               
                //Debug assert statements
                assert(elements > 0);
                assert(startingPitch+elements < everyNote.length);
               
                switch(currentScale)
                {
                        //The beauty of this method is that we can adopt it automatically between ANY future scale type.
                        //All you have to do is change the scaleBuildHelper
                        case pentatonic_major:
                        {
                                int[] scaleBuildHelper = {2,2,3,2,3};           //This defines the positive difference between notes in a scale
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case pentatonic_minor:
                        {
                                int[] scaleBuildHelper = {3,2,2,3,2};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case major:
                        {
                                int[] scaleBuildHelper = {2,2,1,2,2,2,1};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case minor_natural:
                        {
                                int[] scaleBuildHelper = {2,1,2,2,1,2,2};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case minor_harmonic:
                        {
                                int[] scaleBuildHelper = {2,1,2,2,1,3,1};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case minor_melodic:     //This is an interesting scale because it's different on the way down.
                        {
                                int[] scaleBuildHelper = {2,1,2,2,2,2,1, -2, -2};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case gypsy_minor:
                        {
                                int[] scaleBuildHelper = {2,1,3,1,1,3,1};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                        case gypsy_major:
                        {
                                int[] scaleBuildHelper = {1,3,1,2,1,3,1};
                                buildScale(scaleBuildHelper, elements);
                                break;
                        }
                }
                return ret;
        }
        
        //This function actually builds the 'scale', and loads the array ret with the names of each note within the selected scale/octave/key
        private void buildScale(int [] scaleBuildHelper, int elements)
        {
                int i=1, j=0;
                while(i<elements)
                {
                	try{
                        ret[i] = everyNote[ret[i-1].getPitch() + scaleBuildHelper[j]];
                        i++;
                       
                        if(j<scaleBuildHelper.length-1){ j++;}
                        else {j = 0;}      
                }
                //In the case that the user tries to load a scale/note outside of the array of 88 notes, the program will catch the exception and simply repeat the last valid note for any remaining keys
                catch (ArrayIndexOutOfBoundsException e){
                	System.out.println("Out of Bounds on Build");
                	ret[i] = everyNote[ret[i-1].getPitch()];
                	i++;
                }
               }
       
}
}

