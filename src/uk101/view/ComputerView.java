/**
 * Compukit UK101 Simulator
 *
 * (C) Copyright Tim Baldwin 2010
 */
package uk101.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.beans.PropertyVetoException;
import java.io.File;

import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.filechooser.FileFilter;

import uk101.machine.Computer;
import uk101.machine.Configuration;
import uk101.view.component.ImageFormat;

/**
 * A visual representation of the full computer.  This is a frame that contains the
 * visuals for the various computer elements.
 *
 * @author Baldwin
 */
public class ComputerView extends JDesktopPane implements ActionListener {
    private static final long serialVersionUID = 1L;

    static final String IMAGE_LOAD = "Load...";
    static final String IMAGE_SAVE = "Save...";

    Computer computer;
    JFileChooser imageSelect;

    MachineView machine;
    VideoView video;
    KeyboardView keyboard;
    CassetteView cassette;

    public ComputerView(Computer computer, Configuration cfg) {
        setLayout(null);

        this.computer = computer;

        // Create views for the various machine elements
        video = new VideoView(computer.video, cfg);
        keyboard = new KeyboardView(computer, computer.keyboard);
        cassette = new CassetteView(computer.recorder);
        machine = new MachineView(computer, this);
        
        // Attach the keyboard handler to each top level frame
        attachKeyboard(video, keyboard);
        attachKeyboard(keyboard, keyboard);
        attachKeyboard(cassette, keyboard);
        attachKeyboard(machine, keyboard);

        add(machine);
        add(cassette);
        add(video);     // Add video and keyboard last to make sure
        add(keyboard);  // they appear on top.

        // Create a file chooser dialog for the load/save image function
        imageSelect = new JFileChooser(new File("."));
        imageSelect.setDialogTitle("UK101 Machine Image");
        imageSelect.setFileFilter(new FileFilter() {
            public boolean accept(File f) {
                return (f.isDirectory() || f.getName().endsWith(".uk101"));
            }
            public String getDescription() {
                return "UK101 Machine Images";
            }
        });
    }

    // Layout all the windows in their default positions
    public boolean defaultLayout() {
        int maxX1 = video.getWidth() + machine.getWidth() + 5;
        int maxX2 = keyboard.getWidth() + cassette.getWidth() + 10;
        int maxX = Math.max(maxX1, maxX2);
        int maxY = keyboard.getHeight() + video.getHeight();

        int vidX = 0, vidY = 0;
        if (video.getWidth() < keyboard.getWidth()) {
            vidX = (keyboard.getWidth() - video.getWidth())/2;
        }
        video.setLocation(vidX, vidY);

        int kybX = 0, kybY = maxY - keyboard.getHeight();
        if (keyboard.getWidth() < video.getWidth()) {
            kybX = (video.getWidth() - keyboard.getWidth())/2;
        }
        keyboard.setLocation(kybX, kybY);

        int casX = kybX + keyboard.getWidth() + 10, casY = maxY - cassette.getHeight();
        cassette.setLocation(casX, casY);

        int macX = maxX - machine.getWidth(), macY = 0;
        machine.setLocation(macX, macY);

        Dimension size = new Dimension(maxX, maxY);
        setPreferredSize(size);

        return false;       // Layout is incomplete (frame is unsized)
    }

    // Attach a keyListener to a top level window and ensure that no 
    // subcomponents can grab focus.
    private void attachKeyboard(JInternalFrame frame, KeyListener listener) {
        frame.setFocusable(true);
        frame.addKeyListener(listener);
        for (Component c : frame.getComponents()) {
            removeFocus(c);
        }
    }
    
    private void removeFocus(Component c) {
        c.setFocusable(false);
        if (c instanceof Container) {
            for (Component cc : ((Container)c).getComponents()) {
                removeFocus(cc);
            }
        }
    }
    
    // Set focus to the keyboard
    public void focusKeyboard() {
        try {
            keyboard.setSelected(true);
        } catch (PropertyVetoException e) {
        }
    }

    /*
     * Prompt to load and save machine images
     */

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(IMAGE_LOAD)) {
            if (imageSelect.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = imageSelect.getSelectedFile();
                MachineImage image = MachineImage.readImage(file);
                image.apply(computer, this);
            }
        } else if (e.getActionCommand().equals(IMAGE_SAVE)) {
            ImageFormat format = new ImageFormat();
            imageSelect.setAccessory(format);
            if (imageSelect.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = imageSelect.getSelectedFile();
                if (!file.getName().contains("."))
                    file = new File(file.getPath() + ".uk101");
                ComputerView saveView = format.savePostions() ? this : null;
                Configuration saveCfg = format.saveProperties() ? computer.config : null;
                MachineImage image = new MachineImage(computer, saveView, saveCfg);
                image.write(file);
            }
        }
    }
}
