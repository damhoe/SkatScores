package com.damhoe.scoresheetskat.game.adapter.in.ui.shared;

import com.damhoe.scoresheetskat.game.domain.SkatGamePreview;

public interface GamePreviewItemClickListener {
 void notifyDelete(SkatGamePreview skatGamePreview);
 void notifySelect(SkatGamePreview skatGamePreview);
}
