package com.damhoe.skatscores.game.adapter.presentation.shared;

import com.damhoe.skatscores.game.domain.skat.SkatGamePreview;

public interface GamePreviewItemClickListener {
 void notifyDelete(SkatGamePreview skatGamePreview);
 void notifySelect(SkatGamePreview skatGamePreview);
}
