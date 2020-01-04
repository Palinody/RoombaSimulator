package Utils;

import java.awt.Color;
import java.util.ArrayList;

/**
 * RGBA color palette
 * */
public class ColorPalette {

    private int[] _color_palette;
    private int _alpha = 200;
    /**
     * @param alpha set alpha [0, 255]
     */
    public ColorPalette(int alpha){
        _alpha = alpha;
        int n_colors = 2;
        _color_palette = new int[n_colors * n_colors * 255];
        for(int i = 0; i < n_colors * 255; ++i) {
            if (i < 255) {
                _color_palette[i * n_colors] = i; // 0 + i * n_colors
                _color_palette[1 + i * n_colors] = 255;
            } else {
                _color_palette[i * n_colors] = 255; // 0 + i * n_colors
                _color_palette[1 + i * n_colors] = n_colors * 255 - i;
            }
        }
    }

    public void setAlpha(int new_alpha){ _alpha = new_alpha; }

    /**
     * @param proba value in range [0, 1)
     * @return custom color based on param value
     * */
    public Color getHeatMapColorFromProbability(float proba){
        int index = (int)(proba * _color_palette.length / 2);
        int r = _color_palette[1 + index * 2];
        int g = _color_palette[index * 2];
        return new Color(r, g, 0, _alpha);
    }
}
