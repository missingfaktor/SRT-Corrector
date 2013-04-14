SRT Corrector
==============

The Subrip subtitle files (SRT extension) that we get online are sometimes mismatched with the videos we have. They're either ahead in time or somewhat behind. Now our trusted VLC player has keyboad shortcuts viz., <kbd>g</kbd> and <kbd>h</kbd> that let us shift the subtitles on time scale. But it would be much nicer if we could modify the SRT file itself so that when someone uses it in future, they won't have to do this <kbd>g</kbd>-<kbd>h</kbd> exercise. This little script allows you to do just that.

**Use:** java SrtCorrector \<input-file\> \<time-adjustment-in-hh:mm:ss:msc-format\> \<output-file\>

(All testing was done at REPL. In other words, no tests were written.)