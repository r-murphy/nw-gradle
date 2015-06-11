
for f in `find . -name '*.java'`; do uncrustify -c uncrustify.cfg -f $f -o $f; done;
for f in `find . -name '*.java.unc-backup*'`; do rm $f; done;
