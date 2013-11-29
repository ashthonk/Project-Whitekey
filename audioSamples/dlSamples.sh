curl -L -O http://theremin.music.uiowa.edu/sound%20files/MIS/Piano_Other/piano/Piano.mf.[A-F]b[0-8].aiff
curl -L -O http://theremin.music.uiowa.edu/sound%20files/MIS/Piano_Other/piano/Piano.mf.[A-F][0-8].aiff
find . -size 3 -delete
mkdir piano
cp *.aiff ./piano

cd piano
for i in $( ls -R | grep .aiff$ ); do
echo "Working on $i"
sox -V $i -t .mp3 -r 8000 $i_processed.mp3
done

for j in $( ls -R | grep .mp3$ ); do
sox $j $j silence 1 0.1 1%
done
