package com.damhoe.skatscores.game.adapter.in.ui.shared;

import com.damhoe.skatscores.game.domain.SkatGamePreview;

public interface GamePreviewItemClickListener {
 void notifyDelete(SkatGamePreview skatGamePreview);
 void notifySelect(SkatGamePreview skatGamePreview);
}
