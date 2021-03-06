/*
 * Copyright (c) 2019 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.hansolo.spacefx;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class SpaceFX extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        final SpaceFXView view   = new SpaceFXView();
        final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        final double      width  = SpaceFXView.WIDTH > bounds.getWidth() ? bounds.getWidth() : SpaceFXView.WIDTH;
        final double      height = SpaceFXView.HEIGHT > bounds.getHeight() ? bounds.getHeight() : SpaceFXView.HEIGHT;
        Scene scene = new Scene(view, width, height);
        stage.setScene(scene);
        stage.show();

        view.registerListeners();
    }

    public static void main(String[] args) {
        launch(args);
    }
}