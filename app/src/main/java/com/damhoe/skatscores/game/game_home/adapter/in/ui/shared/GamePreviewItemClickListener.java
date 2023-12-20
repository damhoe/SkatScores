package com.damhoe.skatscores.game.game_home.adapter.in.ui.shared;

import com.damhoe.skatscores.game.game_home.domain.SkatGamePreview;

public interface GamePreviewItemClickListener {
 void notifyDelete(SkatGamePreview skatGamePreview);
 void notifySelect(SkatGamePreview skatGamePreview);
}
