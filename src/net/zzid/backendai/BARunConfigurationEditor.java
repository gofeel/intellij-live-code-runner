package net.zzid.backendai;

import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BARunConfigurationEditor extends SettingsEditor<BARunConfiguration> implements ActionListener {
    private final BARunConfiguration runConfiguration;
    private JPanel mainPanel;
    private TextFieldWithBrowseButton scriptTextField;
    private JTextField accessKeyField;
    private JTextField secretKeyField;
    private JComboBox kernelTypeComboBox;


    BARunConfigurationEditor(Project project, BARunConfiguration runConfiguration) {
        this.runConfiguration = runConfiguration;
        FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
        scriptTextField.addBrowseFolderListener("Choose Script", "Please choose script to run", project, descriptor);
        kernelTypeComboBox.addActionListener( this );
        //TODO: ind some better way to register.

        kernelTypeComboBox.addItem(new Item("auto", "<<<Auto>>>"));
        kernelTypeComboBox.addItem(new Item("python3", "Python 3"));
        kernelTypeComboBox.addItem(new Item("tensorflow-python3-gpu", "Python 3 with TensorFlow/Keras"));
        kernelTypeComboBox.addItem(new Item("python3-theano", "Python 3 with Theano"));
        kernelTypeComboBox.addItem(new Item("python3-caffe", "Python 3 with Caffe"));
        kernelTypeComboBox.addItem(new Item("python2", "Python 2"));
        kernelTypeComboBox.addItem(new Item("r3", "R"));
        kernelTypeComboBox.addItem(new Item("julia", "Julia"));
        kernelTypeComboBox.addItem(new Item("nodejs4", "Java Script"));
        kernelTypeComboBox.addItem(new Item("php7", "PHP 7"));
        kernelTypeComboBox.addItem(new Item("octave4", "Octave / Matlab"));
        kernelTypeComboBox.addItem(new Item("haskell", "Haskell"));
        kernelTypeComboBox.setMaximumRowCount(15);
    }

    @Override
    protected void resetEditorFrom(BARunConfiguration runConfiguration) {
        String scriptPath = runConfiguration.getScriptPath();
        String accessKey = runConfiguration.getAccessKey();
        String secretKey = runConfiguration.getSecretKey();
        String kernelType = runConfiguration.getKernelType();

        if (!StringUtil.isEmpty(scriptPath)) {
            scriptTextField.setText(scriptPath);
            String[] parts = scriptPath.split("/");
            if (parts.length > 0) {
                runConfiguration.setName(parts[parts.length - 1]);
            }
        }
        if (!StringUtil.isEmpty(accessKey)) {
            accessKeyField.setText(accessKey);
        }
        if (!StringUtil.isEmpty(secretKey)) {
            secretKeyField.setText(secretKey);
        }

        if (!StringUtil.isEmpty(kernelType)) {
            this.setSelectedValue(kernelTypeComboBox, kernelType);
        }
    }

    @Override
    protected void applyEditorTo(BARunConfiguration runConfiguration) throws ConfigurationException {
        runConfiguration.setScriptPath(scriptTextField.getText().trim());
        runConfiguration.setAccessKey(accessKeyField.getText().trim());
        runConfiguration.setSecretKey(secretKeyField.getText().trim());
        Item item = (Item) kernelTypeComboBox.getSelectedItem();
        runConfiguration.setKernelType(item.getId());
    }

    @Override
    @NotNull
    protected JComponent createEditor() {
        return mainPanel;
    }

    @Override
    protected void disposeEditor() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    public void setSelectedValue(JComboBox comboBox, String value)
    {
        Item item;
        for (int i = 0; i < comboBox.getItemCount(); i++)
        {
            item = (Item)comboBox.getItemAt(i);
            if (item.getId().equals(value))
            {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    class Item {

        private String id;
        private String description;

        public Item(String id, String description) {
            this.id = id;
            this.description = description;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
