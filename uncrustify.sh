
for f in `find . -name '*.java'`; do uncrustify -c uncrustify.cfg -f $f -o $f; done;
#for f in `find . -name '*.groovy'`; do uncrustify -c uncrustify.cfg -f $f -o $f; done;
for f in `find . -name '*.unc-backup*'`; do rm $f; done;
