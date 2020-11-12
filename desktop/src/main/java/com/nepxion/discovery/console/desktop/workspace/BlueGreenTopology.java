package com.nepxion.discovery.console.desktop.workspace;

/**
 * <p>Title: Nepxion Discovery</p>
 * <p>Description: Nepxion Discovery</p>
 * <p>Copyright: Copyright (c) 2017-2050</p>
 * <p>Company: Nepxion</p>
 * @author Haojun Ren
 * @version 1.0
 */

import twaver.Link;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.nepxion.cots.twaver.element.TElementManager;
import com.nepxion.cots.twaver.element.TLink;
import com.nepxion.cots.twaver.element.TNode;
import com.nepxion.discovery.common.constant.DiscoveryConstant;
import com.nepxion.discovery.console.controller.ServiceController;
import com.nepxion.discovery.console.desktop.icon.ConsoleIconFactory;
import com.nepxion.discovery.console.desktop.locale.ConsoleLocaleFactory;
import com.nepxion.discovery.console.desktop.topology.LinkUI;
import com.nepxion.discovery.console.desktop.topology.NodeImageType;
import com.nepxion.discovery.console.desktop.topology.NodeLocation;
import com.nepxion.discovery.console.desktop.topology.NodeSizeType;
import com.nepxion.discovery.console.desktop.topology.NodeUI;
import com.nepxion.discovery.console.desktop.workspace.panel.ReleasePanel;
import com.nepxion.discovery.console.desktop.workspace.processor.BlueGreenStrategyProcessor;
import com.nepxion.discovery.console.desktop.workspace.processor.StrategyProcessor;
import com.nepxion.discovery.console.desktop.workspace.type.ConfigType;
import com.nepxion.discovery.console.desktop.workspace.type.DeployType;
import com.nepxion.discovery.console.desktop.workspace.type.LinkType;
import com.nepxion.discovery.console.desktop.workspace.type.NodeType;
import com.nepxion.discovery.console.desktop.workspace.type.ReleaseType;
import com.nepxion.discovery.console.desktop.workspace.type.StrategyType;
import com.nepxion.discovery.console.entity.Instance;
import com.nepxion.swing.action.JSecurityAction;
import com.nepxion.swing.button.ButtonManager;
import com.nepxion.swing.button.JClassicButton;
import com.nepxion.swing.combobox.JBasicComboBox;
import com.nepxion.swing.dialog.JExceptionDialog;
import com.nepxion.swing.element.ElementNode;
import com.nepxion.swing.handle.HandleManager;
import com.nepxion.swing.icon.IconFactory;
import com.nepxion.swing.label.JBasicLabel;
import com.nepxion.swing.layout.filed.FiledLayout;
import com.nepxion.swing.layout.table.TableLayout;
import com.nepxion.swing.locale.SwingLocale;
import com.nepxion.swing.optionpane.JBasicOptionPane;
import com.nepxion.swing.selector.checkbox.JCheckBoxSelector;
import com.nepxion.swing.textfield.JBasicTextField;

public class BlueGreenTopology extends AbstractTopology {
    private static final long serialVersionUID = 1L;

    private NodeLocation nodeLocation = new NodeLocation(440, 100, 200, 0);
    private NodeUI serviceYellowNodeUI = new NodeUI(NodeImageType.SERVICE_YELLOW, NodeSizeType.MIDDLE, true);
    private NodeUI serviceBlueNodeUI = new NodeUI(NodeImageType.SERVICE_BLUE, NodeSizeType.MIDDLE, true);
    private NodeUI serviceGreenNodeUI = new NodeUI(NodeImageType.SERVICE_GREEN, NodeSizeType.MIDDLE, true);
    private NodeUI gatewayBlackNodeUI = new NodeUI(NodeImageType.GATEWAY_BLACK, NodeSizeType.LARGE, true);

    private JBasicComboBox serviceIdComboBox;
    private JBasicComboBox blueMetadataComboBox;
    private JBasicComboBox greenMetadataComboBox;
    private JBasicComboBox basicMetadataComboBox;
    private JBasicTextField blueConditionTextField;
    private JBasicTextField greenConditionTextField;

    private TNode gatewayNode;
    private TNode blueNode;
    private TNode greenNode;
    private TNode basicNode;

    private String group;
    private Instance gateway;
    private DeployType deployType;

    private Object[] serviceIds;

    private StrategyProcessor strategyProcessor = new BlueGreenStrategyProcessor();

    public BlueGreenTopology() {
        super();

        initializeContentBar();
    }

    private void initializeContentBar() {
        serviceIdComboBox = new JBasicComboBox();
        serviceIdComboBox.setEditable(true);
        serviceIdComboBox.setPreferredSize(new Dimension(300, layoutTextField.getPreferredSize().height));
        serviceIdComboBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (serviceIdComboBox.getSelectedItem() != e.getItem()) {
                    setMetadataUI();
                }
            }
        });
        JClassicButton refreshServicesButton = new JClassicButton(createRefreshServiceListAction());
        refreshServicesButton.setPreferredSize(new Dimension(30, refreshServicesButton.getPreferredSize().height));

        blueMetadataComboBox = new JBasicComboBox();
        blueMetadataComboBox.setEditable(true);
        JClassicButton blueMetadataButton = new JClassicButton(createMetadataSelectorAction(blueMetadataComboBox));
        blueMetadataButton.setPreferredSize(new Dimension(30, blueMetadataButton.getPreferredSize().height));

        greenMetadataComboBox = new JBasicComboBox();
        greenMetadataComboBox.setEditable(true);
        JClassicButton greenMetadataButton = new JClassicButton(createMetadataSelectorAction(greenMetadataComboBox));
        greenMetadataButton.setPreferredSize(new Dimension(30, greenMetadataButton.getPreferredSize().height));

        basicMetadataComboBox = new JBasicComboBox();
        basicMetadataComboBox.setEditable(true);
        JClassicButton basicMetadataButton = new JClassicButton(createMetadataSelectorAction(basicMetadataComboBox));
        basicMetadataButton.setPreferredSize(new Dimension(30, basicMetadataButton.getPreferredSize().height));

        double[][] serviceSize = {
                { TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED },
                { TableLayout.PREFERRED }
        };

        TableLayout serviceTableLayout = new TableLayout(serviceSize);
        serviceTableLayout.setHGap(5);
        serviceTableLayout.setVGap(5);

        JPanel servicePanel = new JPanel();
        servicePanel.setLayout(serviceTableLayout);
        servicePanel.add(serviceIdComboBox, "0, 0");
        servicePanel.add(refreshServicesButton, "1, 0");
        servicePanel.add(new JBasicLabel("蓝版本"), "2, 0");
        servicePanel.add(blueMetadataComboBox, "3, 0");
        servicePanel.add(blueMetadataButton, "4, 0");
        servicePanel.add(new JBasicLabel("绿版本"), "5, 0");
        servicePanel.add(greenMetadataComboBox, "6, 0");
        servicePanel.add(greenMetadataButton, "7, 0");
        servicePanel.add(new JBasicLabel("兜底版本"), "8, 0");
        servicePanel.add(basicMetadataComboBox, "9, 0");
        servicePanel.add(basicMetadataButton, "10, 0");

        JPanel serviceToolBar = new JPanel();
        serviceToolBar.setLayout(new FiledLayout(FiledLayout.ROW, FiledLayout.FULL, 0));
        serviceToolBar.add(new JClassicButton(createAddServiceStrategyAction()));
        serviceToolBar.add(new JClassicButton(createRemoveServiceStrategyAction()));
        serviceToolBar.add(new JClassicButton(createModifyServiceStrategyAction()));

        blueConditionTextField = new JBasicTextField("#H['a'] == '1' && #H['b'] <= '2'");
        greenConditionTextField = new JBasicTextField("#H['a'] == '3'");

        double[][] conditionSize = {
                { TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL },
                { TableLayout.PREFERRED }
        };

        TableLayout conditionTableLayout = new TableLayout(conditionSize);
        conditionTableLayout.setHGap(5);
        conditionTableLayout.setVGap(5);

        JPanel conditionPanel = new JPanel();
        conditionPanel.setLayout(conditionTableLayout);
        conditionPanel.add(new JBasicLabel("蓝条件"), "0, 0");
        conditionPanel.add(blueConditionTextField, "1, 0");
        conditionPanel.add(new JBasicLabel("绿条件"), "2, 0");
        conditionPanel.add(greenConditionTextField, "3, 0");

        JPanel conditionToolBar = new JPanel();
        conditionToolBar.setLayout(new FiledLayout(FiledLayout.ROW, FiledLayout.FULL, 0));
        conditionToolBar.add(new JClassicButton(createValidateConditionAction()));
        conditionToolBar.add(new JClassicButton(createModifyConditionAction()));

        double[][] size = {
                { TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED },
                { TableLayout.PREFERRED, TableLayout.PREFERRED }
        };

        TableLayout tableLayout = new TableLayout(size);
        tableLayout.setHGap(10);
        tableLayout.setVGap(5);

        JPanel toolBar = new JPanel();
        toolBar.setLayout(tableLayout);
        toolBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        toolBar.add(new JBasicLabel("蓝绿服务"), "0, 0");
        toolBar.add(servicePanel, "1, 0");
        toolBar.add(serviceToolBar, "2, 0");
        toolBar.add(new JBasicLabel("蓝绿条件"), "0, 1");
        toolBar.add(conditionPanel, "1, 1");
        toolBar.add(conditionToolBar, "2, 1");

        add(toolBar, BorderLayout.NORTH);
    }

    public void initializeData(String group, Instance gateway, ReleaseType releaseType, StrategyType strategyType, ConfigType configType, DeployType deployType) {
        this.group = group;
        this.gateway = gateway;
        this.releaseType = releaseType;
        this.strategyType = strategyType;
        this.configType = configType;
        this.deployType = deployType;

        refreshData();
    }

    public void refreshData() {
        try {
            if (deployType == DeployType.DOMAIN_GATEWAY) {
                this.serviceIds = ServiceController.getInstanceMap(Arrays.asList(group)).keySet().toArray();
            } else {
                this.serviceIds = ServiceController.getServices().toArray();
            }
        } catch (Exception e) {
            JExceptionDialog.traceException(HandleManager.getFrame(this), ConsoleLocaleFactory.getString("query_data_failure"), e);
        }
    }

    public List<Instance> getInstances(String serviceId) {
        try {
            return ServiceController.getInstanceList(serviceId);
        } catch (Exception e) {
            JExceptionDialog.traceException(HandleManager.getFrame(this), ConsoleLocaleFactory.getString("query_data_failure"), e);
        }

        return null;
    }

    public void initializeUI() {
        setTitle();
        setGatewayNode();

        refreshUI();
    }

    public void refreshUI() {
        setServiceUI();
        setMetadataUI();
    }

    private void setTitle() {
        background.setTitle(releaseType.getDescription() + " | " + strategyType.getDescription() + " | " + configType.getDescription() + " | " + deployType.getDescription());
    }

    private void setGatewayNode() {
        dataBox.clear();

        blueNode = null;
        greenNode = null;
        basicNode = null;

        gatewayNode = addNode(ButtonManager.getHtmlText(gateway.getServiceId() + "\n" + group), gatewayBlackNodeUI);
        gatewayNode.setUserObject(gateway);
        gatewayNode.setBusinessObject(NodeType.GATEWAY);

        setNodeTopBottom(gatewayNode, false);
    }

    @SuppressWarnings("unchecked")
    private void setServiceUI() {
        serviceIdComboBox.setModel(new DefaultComboBoxModel<>(serviceIds));
    }

    @SuppressWarnings("unchecked")
    private void setMetadataUI() {
        List<String> metadatas = new ArrayList<String>();
        Object selectedItem = serviceIdComboBox.getSelectedItem();
        if (selectedItem != null) {
            String serviceId = selectedItem.toString().trim();
            List<Instance> instances = getInstances(serviceId);
            if (CollectionUtils.isNotEmpty(instances)) {
                for (Instance instance : instances) {
                    String metadata = instance.getMetadata().get(strategyType.toString());
                    if (StringUtils.isNotBlank(metadata)) {
                        metadatas.add(metadata);
                    }
                }
            }
        }
        metadatas.add(DiscoveryConstant.DEFAULT);

        blueMetadataComboBox.setModel(new DefaultComboBoxModel<>(metadatas.toArray()));
        greenMetadataComboBox.setModel(new DefaultComboBoxModel<>(metadatas.toArray()));
        basicMetadataComboBox.setModel(new DefaultComboBoxModel<>(metadatas.toArray()));
    }

    private void addNodes(String serviceId, String blueMetadata, String greenMetadata, String basicMetadata, String blueCondition, String greenCondition) {
        TNode newBlueNode = addNode(ButtonManager.getHtmlText(serviceId + "\n" + strategyType.toString() + "=" + blueMetadata), serviceBlueNodeUI);
        Instance newBlueInstance = new Instance();
        newBlueInstance.setServiceId(serviceId);
        Map<String, String> newBlueMetadataMap = new HashMap<String, String>();
        newBlueMetadataMap.put(strategyType.toString(), blueMetadata);
        newBlueInstance.setMetadata(newBlueMetadataMap);
        newBlueNode.setUserObject(newBlueInstance);
        newBlueNode.setBusinessObject(NodeType.BLUE);
        if (blueNode == null) {
            TLink blueLink = addLink(gatewayNode, newBlueNode, LinkUI.BLUE);
            blueLink.setDisplayName("蓝版本路由");
            blueLink.setToolTipText(blueCondition);
            blueLink.setUserObject(blueCondition);
            blueLink.setBusinessObject(LinkType.BLUE);
        } else {
            TLink link = addLink(blueNode, newBlueNode, null);
            link.setBusinessObject(LinkType.UNDEFINED);
        }
        blueNode = newBlueNode;

        TNode newGreenNode = addNode(ButtonManager.getHtmlText(serviceId + "\n" + strategyType.toString() + "=" + greenMetadata), serviceGreenNodeUI);
        Instance newGreenInstance = new Instance();
        newGreenInstance.setServiceId(serviceId);
        Map<String, String> newGreenMetadataMap = new HashMap<String, String>();
        newGreenMetadataMap.put(strategyType.toString(), greenMetadata);
        newGreenInstance.setMetadata(newGreenMetadataMap);
        newGreenNode.setUserObject(newGreenInstance);
        newGreenNode.setBusinessObject(NodeType.GREEN);
        if (greenNode == null) {
            TLink greenLink = addLink(gatewayNode, newGreenNode, LinkUI.GREEN);
            greenLink.setDisplayName("绿版本路由");
            greenLink.setToolTipText(greenCondition);
            greenLink.setUserObject(greenCondition);
            greenLink.setBusinessObject(LinkType.GREEN);
        } else {
            TLink link = addLink(greenNode, newGreenNode, null);
            link.setBusinessObject(LinkType.UNDEFINED);
        }
        greenNode = newGreenNode;

        TNode newBasicNode = addNode(ButtonManager.getHtmlText(serviceId + "\n" + strategyType.toString() + "=" + basicMetadata), serviceYellowNodeUI);
        Instance newBasicInstance = new Instance();
        newBasicInstance.setServiceId(serviceId);
        Map<String, String> newBasicMetadataMap = new HashMap<String, String>();
        newBasicMetadataMap.put(strategyType.toString(), basicMetadata);
        newBasicInstance.setMetadata(newBasicMetadataMap);
        newBasicNode.setUserObject(newBasicInstance);
        newBasicNode.setBusinessObject(NodeType.BASIC);
        if (basicNode == null) {
            TLink basicLink = addLink(gatewayNode, newBasicNode, LinkUI.YELLOW);
            basicLink.setDisplayName("兜底路由");
            basicLink.setBusinessObject(LinkType.BASIC);
        } else {
            TLink link = addLink(basicNode, newBasicNode, null);
            link.setBusinessObject(LinkType.UNDEFINED);
        }
        basicNode = newBasicNode;
    }

    @SuppressWarnings("unchecked")
    private void removeNodes() {
        if (blueNode != null) {
            List<Link> blueLinks = blueNode.getAllLinks();
            if (CollectionUtils.isNotEmpty(blueLinks)) {
                TNode currentBlueNode = (TNode) blueLinks.get(0).getFrom();
                dataBox.removeElement(blueNode);
                if (currentBlueNode != gatewayNode) {
                    blueNode = currentBlueNode;
                } else {
                    blueNode = null;
                }
            }
        }

        if (greenNode != null) {
            List<Link> greenLinks = greenNode.getAllLinks();
            if (CollectionUtils.isNotEmpty(greenLinks)) {
                TNode currentGreenNode = (TNode) greenLinks.get(0).getFrom();
                dataBox.removeElement(greenNode);
                if (currentGreenNode != gatewayNode) {
                    greenNode = currentGreenNode;
                } else {
                    greenNode = null;
                }
            }
        }

        if (basicNode != null) {
            List<Link> basicLinks = basicNode.getAllLinks();
            if (CollectionUtils.isNotEmpty(basicLinks)) {
                TNode currentBasicNode = (TNode) basicLinks.get(0).getFrom();
                dataBox.removeElement(basicNode);
                if (currentBasicNode != gatewayNode) {
                    basicNode = currentBasicNode;
                } else {
                    basicNode = null;
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked", "incomplete-switch" })
    private void modifyNodes(String serviceId, String blueMetadata, String greenMetadata, String basicMetadata) {
        List<TNode> nodes = TElementManager.getNodes(dataBox);
        for (TNode node : nodes) {
            Instance instance = (Instance) node.getUserObject();
            if (StringUtils.equalsIgnoreCase(instance.getServiceId(), serviceId)) {
                NodeType nodeType = (NodeType) node.getBusinessObject();
                switch (nodeType) {
                    case BLUE:
                        node.setName(ButtonManager.getHtmlText(serviceId + "\n" + strategyType.toString() + "=" + blueMetadata));
                        instance.getMetadata().put(strategyType.toString(), blueMetadata);
                        break;
                    case GREEN:
                        node.setName(ButtonManager.getHtmlText(serviceId + "\n" + strategyType.toString() + "=" + greenMetadata));
                        instance.getMetadata().put(strategyType.toString(), greenMetadata);
                        break;
                    case BASIC:
                        node.setName(ButtonManager.getHtmlText(serviceId + "\n" + strategyType.toString() + "=" + basicMetadata));
                        instance.getMetadata().put(strategyType.toString(), basicMetadata);
                        break;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private boolean hasNodes(String serviceId) {
        List<TNode> nodes = TElementManager.getNodes(dataBox);
        for (TNode node : nodes) {
            Instance instance = (Instance) node.getUserObject();
            if (StringUtils.equalsIgnoreCase(instance.getServiceId(), serviceId)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings({ "unchecked", "incomplete-switch" })
    private void modifyLinks(String blueCondition, String greenCondition) {
        List<TLink> links = TElementManager.getLinks(dataBox);
        for (TLink link : links) {
            LinkType linkType = (LinkType) link.getBusinessObject();
            switch (linkType) {
                case BLUE:
                    link.setToolTipText(blueCondition);
                    link.setUserObject(blueCondition);
                    break;
                case GREEN:
                    link.setToolTipText(greenCondition);
                    link.setUserObject(greenCondition);
                    break;
            }
        }
    }

    private TNode addNode(String name, NodeUI topologyEntity) {
        TNode node = createNode(name, topologyEntity, nodeLocation, 0);

        dataBox.addElement(node);

        return node;
    }

    @SuppressWarnings("unchecked")
    private TLink addLink(TNode fromNode, TNode toNode, Color linkFlowingColor) {
        List<TLink> links = TElementManager.getLinks(dataBox);
        for (TLink link : links) {
            if (link.getFrom() == fromNode && link.getTo() == toNode) {
                return null;
            }
        }

        TLink link = createLink(fromNode, toNode, linkFlowingColor != null);
        if (linkFlowingColor != null) {
            link.putLinkToArrowColor(Color.yellow);
            link.putLinkFlowing(true);
            link.putLinkFlowingColor(linkFlowingColor);
            link.putLinkFlowingWidth(3);
        }

        dataBox.addElement(link);

        return link;
    }

    private JSecurityAction createRefreshServiceListAction() {
        JSecurityAction action = new JSecurityAction(ConsoleIconFactory.getSwingIcon("refresh.png"), "刷新服务列表") {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                refreshData();
                refreshUI();
            }
        };

        return action;
    }

    private JSecurityAction createMetadataSelectorAction(JBasicComboBox metadataComboBox) {
        JSecurityAction action = new JSecurityAction(ConsoleIconFactory.getSwingIcon("direction_south.png"), "版本选取") {
            private static final long serialVersionUID = 1L;

            @SuppressWarnings({ "unchecked", "rawtypes" })
            public void execute(ActionEvent e) {
                ComboBoxModel metadataComboBoxModel = metadataComboBox.getModel();
                List<ElementNode> metadataElementNodes = new ArrayList<ElementNode>();
                for (int i = 0; i < metadataComboBoxModel.getSize(); i++) {
                    String metadata = metadataComboBoxModel.getElementAt(i).toString();
                    metadataElementNodes.add(new ElementNode(metadata, IconFactory.getSwingIcon("component/file_chooser_16.png"), metadata, metadata));
                }

                JCheckBoxSelector checkBoxSelector = new JCheckBoxSelector(HandleManager.getFrame(BlueGreenTopology.this), "版本选取", new Dimension(400, 350), metadataElementNodes);
                checkBoxSelector.setVisible(true);
                checkBoxSelector.dispose();

                if (!checkBoxSelector.isConfirmed()) {
                    return;
                }

                List<String> selectedMetadatas = checkBoxSelector.getSelectedUserObjects();
                if (CollectionUtils.isEmpty(selectedMetadatas)) {
                    return;
                }

                StringBuilder StringBuilder = new StringBuilder();
                int index = 0;
                for (String selectedMetadata : selectedMetadatas) {
                    StringBuilder.append(selectedMetadata);
                    if (index < selectedMetadatas.size() - 1) {
                        StringBuilder.append(DiscoveryConstant.SEPARATE);
                    }

                    index++;
                }

                metadataComboBox.setSelectedItem(StringBuilder.toString());
            }
        };

        return action;
    }

    private JSecurityAction createAddServiceStrategyAction() {
        JSecurityAction action = new JSecurityAction("添加", ConsoleIconFactory.getSwingIcon("add.png"), "添加一层服务策略") {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                if (TElementManager.getNodes(dataBox).size() == 0) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "无入口网关或者服务", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                String serviceId = serviceIdComboBox.getSelectedItem() != null ? serviceIdComboBox.getSelectedItem().toString().trim() : null;
                if (StringUtils.isBlank(serviceId)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "服务名必填", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                if (hasNodes(serviceId)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), serviceId + "已存在", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                String blueMetadata = blueMetadataComboBox.getSelectedItem() != null ? blueMetadataComboBox.getSelectedItem().toString().trim() : null;
                String greenMetadata = greenMetadataComboBox.getSelectedItem() != null ? greenMetadataComboBox.getSelectedItem().toString().trim() : null;
                String basicMetadata = basicMetadataComboBox.getSelectedItem() != null ? basicMetadataComboBox.getSelectedItem().toString().trim() : null;
                String blueCondition = blueConditionTextField.getText().trim();
                String greenCondition = greenConditionTextField.getText().trim();

                if (StringUtils.isBlank(blueMetadata) || StringUtils.isBlank(greenMetadata) || StringUtils.isBlank(basicMetadata)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "版本号必填", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                if (StringUtils.isBlank(blueCondition) || StringUtils.isBlank(greenCondition)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "条件必填", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                addNodes(serviceId, blueMetadata, greenMetadata, basicMetadata, blueCondition, greenCondition);

                layoutActionListener.actionPerformed(null);
            }
        };

        return action;
    }

    private JSecurityAction createRemoveServiceStrategyAction() {
        JSecurityAction action = new JSecurityAction("删除", ConsoleIconFactory.getSwingIcon("delete.png"), "删除一层服务策略") {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                removeNodes();
            }
        };

        return action;
    }

    private JSecurityAction createModifyServiceStrategyAction() {
        JSecurityAction action = new JSecurityAction("修改", ConsoleIconFactory.getSwingIcon("paste.png"), "修改一层服务策略") {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                String serviceId = serviceIdComboBox.getSelectedItem().toString();
                if (StringUtils.isBlank(serviceId)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "服务名必填", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                String blueMetadata = blueMetadataComboBox.getSelectedItem().toString().trim();
                String greenMetadata = greenMetadataComboBox.getSelectedItem().toString().trim();
                String basicMetadata = basicMetadataComboBox.getSelectedItem().toString().trim();

                if (StringUtils.isBlank(blueMetadata) || StringUtils.isBlank(greenMetadata) || StringUtils.isBlank(basicMetadata)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "版本号必填", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                modifyNodes(serviceId, blueMetadata, greenMetadata, basicMetadata);
            }
        };

        return action;
    }

    private JSecurityAction createValidateConditionAction() {
        JSecurityAction action = new JSecurityAction("校验", ConsoleIconFactory.getSwingIcon("config.png"), "校验条件表达式") {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {

            }
        };

        return action;
    }

    private JSecurityAction createModifyConditionAction() {
        JSecurityAction action = new JSecurityAction("修改", ConsoleIconFactory.getSwingIcon("paste.png"), "修改条件表达式") {
            private static final long serialVersionUID = 1L;

            public void execute(ActionEvent e) {
                String blueCondition = blueConditionTextField.getText().trim();
                String greenCondition = greenConditionTextField.getText().trim();

                if (StringUtils.isBlank(blueCondition) || StringUtils.isBlank(greenCondition)) {
                    JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "条件必填", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                    return;
                }

                modifyLinks(blueCondition, greenCondition);
            }
        };

        return action;
    }

    @Override
    public void open() {
        ReleaseType openReleaseType = ReleaseType.BLUE_GREEN;

        ReleasePanel releasePanel = new ReleasePanel(openReleaseType);
        releasePanel.setPreferredSize(new Dimension(480, 180));

        int selectedOption = JBasicOptionPane.showOptionDialog(HandleManager.getFrame(BlueGreenTopology.this), releasePanel, "打开或者新增 [ " + openReleaseType.getDescription() + " ]", JBasicOptionPane.DEFAULT_OPTION, JBasicOptionPane.PLAIN_MESSAGE, ConsoleIconFactory.getSwingIcon("banner/net.png"), new Object[] { SwingLocale.getString("confirm"), SwingLocale.getString("cancel") }, null, true);
        if (selectedOption != 0) {
            return;
        }

        ReleaseType releaseType = releasePanel.getReleaseType();
        StrategyType strategyType = releasePanel.getStrategyType();
        ConfigType configType = releasePanel.getConfigType();
        DeployType deployType = releasePanel.getDeployType();

        String group = releasePanel.getGroup();
        if (StringUtils.isBlank(group)) {
            JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "组不能为空", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

            return;
        }

        String gatewayId = null;
        if (configType == ConfigType.PARTIAL) {
            gatewayId = releasePanel.getGatewayId();
            if (StringUtils.isBlank(gatewayId)) {
                JBasicOptionPane.showMessageDialog(HandleManager.getFrame(BlueGreenTopology.this), "服务名不能为空", SwingLocale.getString("warning"), JBasicOptionPane.WARNING_MESSAGE);

                return;
            }
        }

        Instance gateway = new Instance();
        gateway.setServiceId(gatewayId != null ? gatewayId : "起点服务");
        Map<String, String> metadataMap = new HashMap<String, String>();
        gateway.setMetadata(metadataMap);

        initializeData(group, gateway, releaseType, strategyType, configType, deployType);
        initializeUI();
    }

    @Override
    public void save() {

    }

    @Override
    public void clear() {
        dataBox.clear();
        dataBox.addElement(gatewayNode);

        blueNode = null;
        greenNode = null;
        basicNode = null;
    }

    @Override
    public StrategyProcessor getStrategyProcessor() {
        return strategyProcessor;
    }
}