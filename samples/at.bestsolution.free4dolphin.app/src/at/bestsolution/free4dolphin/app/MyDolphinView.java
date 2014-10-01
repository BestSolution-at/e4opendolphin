package at.bestsolution.free4dolphin.app;

import static at.bestsolution.free4dolphin.app.TutorialConstants.ATT_FIRSTNAME;
import static at.bestsolution.free4dolphin.app.TutorialConstants.CMD_LOG;
import static at.bestsolution.free4dolphin.app.TutorialConstants.PM_PERSON;
import groovy.lang.Closure;

import java.util.List;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransitionBuilder;
import javafx.animation.Transition;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.PaneBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.util.Duration;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.opendolphin.binding.JFXBinder;
import org.opendolphin.core.PresentationModel;
import org.opendolphin.core.client.ClientAttribute;
import org.opendolphin.core.client.ClientDolphin;
import org.opendolphin.core.client.ClientPresentationModel;
import org.opendolphin.core.client.comm.OnFinishedHandlerAdapter;

public class MyDolphinView {
	@Inject
	ClientDolphin clientDolphin;
	
    private TextField textField;
    private Button button;
    private Button reset;
    private static PresentationModel textAttributeModel;
	
	
	@PostConstruct
	void init(BorderPane p) {
		if( textAttributeModel == null ) {
			textAttributeModel = clientDolphin.presentationModel(PM_PERSON, new ClientAttribute(ATT_FIRSTNAME, ""));	
		}
		
		Pane root = PaneBuilder.create().children(
                VBoxBuilder.create().id("content").children(
                        textField = TextFieldBuilder.create().id("firstname").build(),
                        button = ButtonBuilder.create().text("save").build(),
                        reset = ButtonBuilder.create().text("reset").build()
                ).build()
        ).build();

		p.setCenter(root);
		
        addClientSideAction();
        setupBinding();
	}
	
	
	private void setupBinding() {
        JFXBinder.bind("text").of(textField).to(ATT_FIRSTNAME).of(textAttributeModel);
        JFXBinder.bind(ATT_FIRSTNAME).of(textAttributeModel).to("text").of(textField);

        JFXBinder.bindInfo("dirty").of(textAttributeModel).to("style").of(textField, new Closure(null) {
            public String call(Boolean dirty) {
                if (dirty) {
                    textField.getStyleClass().add("dirty");
                } else {
                    textField.getStyleClass().remove("dirty");
                }
                return "";
            }
        });
        Inverter inv = new Inverter();
        JFXBinder.bindInfo("dirty").of(textAttributeModel).to("disabled").of(button, inv);
        JFXBinder.bindInfo("dirty").of(textAttributeModel).to("disabled").of(reset, inv);
    }

    private void addClientSideAction() {
        textField.setOnAction(new RebaseHandler(textAttributeModel));
        button.setOnAction(new RebaseHandler(textAttributeModel));
        final Transition fadeIn = RotateTransitionBuilder.create().node(textField).toAngle(0).duration(Duration.millis(200)).build();
        final Transition fadeOut = RotateTransitionBuilder.create().node(textField).fromAngle(-3).interpolator(Interpolator.LINEAR).
                toAngle(3).cycleCount(3).duration(Duration.millis(100)).
                onFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        textAttributeModel.getAt(ATT_FIRSTNAME).reset();
                        fadeIn.playFromStart();
                    }
                }).build();

        reset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                fadeOut.playFromStart();
            }
        });
    }

    private static class Inverter extends Closure {
        public Inverter() {
            super(null);
        }

        protected Object call(Boolean dirtyState) {
            return !dirtyState;
        }
    }

    private class RebaseHandler implements EventHandler {
        private PresentationModel model;

        public RebaseHandler(PresentationModel model) {
            this.model = model;
        }

        @Override
        public void handle(Event event) {
            clientDolphin.send(CMD_LOG, new OnFinishedHandlerAdapter() {
                @Override
                public void onFinished(List<ClientPresentationModel> presentationModels) {
                    model.getAt(ATT_FIRSTNAME).rebase();
                }
            });
        }

    }	
}
