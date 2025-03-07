package name.abuchen.portfolio.ui.parts;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;

import name.abuchen.portfolio.ui.Images;
import name.abuchen.portfolio.ui.Messages;
import name.abuchen.portfolio.ui.PortfolioPlugin;
import name.abuchen.portfolio.ui.UIConstants;
import name.abuchen.portfolio.ui.util.Colors;
import name.abuchen.portfolio.ui.util.DesktopAPI;
import name.abuchen.portfolio.ui.util.FormDataFactory;
import name.abuchen.portfolio.ui.util.RecentFilesCache;
import name.abuchen.portfolio.util.BuildInfo;

public class WelcomePart
{
    private static final String OPEN = "open:"; //$NON-NLS-1$
    private static final String OPEN_PREFERENCES = "action:opensettings:"; //$NON-NLS-1$

    @Inject
    private ECommandService commandService;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private EPartService partService;

    @Inject
    private RecentFilesCache recentFiles;

    private Composite recentFilesComposite;

    @PostConstruct
    public void createComposite(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NONE);
        container.setBackground(Colors.WHITE);
        GridLayoutFactory.fillDefaults().margins(20, 20).applyTo(container);

        createHeader(container);
        createContent(container);
    }

    private void createHeader(Composite container)
    {
        Composite composite = new Composite(container, SWT.NONE);
        composite.setBackground(container.getBackground());
        composite.setLayout(new FormLayout());

        // logo
        Label image = new Label(composite, SWT.NONE);
        image.setBackground(composite.getBackground());
        image.setImage(Images.LOGO_128.image());

        // name
        Label title = new Label(composite, SWT.NONE);
        title.setText(Messages.LabelPortfolioPerformance);
        title.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.HEADING1);

        // version
        Label version = new Label(composite, SWT.NONE);
        version.setText(PortfolioPlugin.getDefault().getBundle().getVersion().toString() + " (" //$NON-NLS-1$
                        + DateTimeFormatter.ofPattern("MMM yyyy").format(BuildInfo.INSTANCE.getBuildTime()) //$NON-NLS-1$
                        + ")"); //$NON-NLS-1$

        FormDataFactory.startingWith(image) //
                        .thenRight(title, 30).top(new FormAttachment(image, 0, SWT.TOP)) //
                        .thenBelow(version);
    }

    private void createContent(Composite container)
    {
        Composite composite = new Composite(container, SWT.NONE);
        composite.setBackground(container.getBackground());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(composite);

        GridLayoutFactory.fillDefaults().numColumns(3).spacing(20, 20).applyTo(composite);

        createLinks(composite);
        createSettingsInfo(composite);
        createRecentFilesComposite(composite);
    }

    private void createLinks(Composite composite)
    {
        Composite links = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(links);

        GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(links);

        addSectionLabel(links, Messages.IntroLabelActions);
        addLink(links, "action:open", Messages.IntroOpenFile, Messages.IntroOpenFileText); //$NON-NLS-1$
        addLink(links, "action:new", Messages.IntroNewFile, Messages.IntroNewFileText); //$NON-NLS-1$

        addSectionLabel(links, Messages.IntroLabelSamples);
        addLink(links, "action:sample", Messages.IntroOpenSample, Messages.IntroOpenSampleText); //$NON-NLS-1$
        addLink(links, "action:daxsample", Messages.IntroOpenDaxSample, Messages.IntroOpenDaxSampleText); //$NON-NLS-1$
    }

    private void createRecentFilesComposite(Composite actions)
    {
        recentFilesComposite = new Composite(actions, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).applyTo(recentFilesComposite);

        GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(recentFilesComposite);

        addSectionLabel(recentFilesComposite, Messages.IntroLabelRecentlyUsedFiles);

        for (String file : recentFiles.getRecentFiles())
        {
            String name = Path.fromOSString(file).lastSegment();
            addLink(recentFilesComposite, OPEN + file, name, null);
        }
    }

    private void createSettingsInfo(Composite composite)
    {
        Composite section = new Composite(composite, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(section);
        GridLayoutFactory.fillDefaults().margins(5, 5).numColumns(1).applyTo(section);

        addSectionLabel(section, Messages.LabelSettings);

        addLink(section, OPEN_PREFERENCES + "language", Messages.PrefTitleLanguage, null); //$NON-NLS-1$
        addLink(section, OPEN_PREFERENCES + "theme", Messages.LabelTheme + " / " + Messages.LabelFontSize, null); //$NON-NLS-1$ //$NON-NLS-2$
        addLink(section, OPEN_PREFERENCES + "api", Messages.PrefTitleAPIKeys, null); //$NON-NLS-1$

        addSectionLabel(section, Messages.IntroLabelHelp);
        addLink(section, "https://forum.portfolio-performance.info/t/sunny-neues-nennenswertes/23/last", //$NON-NLS-1$
                        Messages.SystemMenuNewAndNoteworthy, Messages.IntroNewAndNoteworthyText);

        addLink(section, "https://forum.portfolio-performance.info", //$NON-NLS-1$
                        Messages.IntroOpenForum, Messages.IntroOpenForumText);
        addLink(section, "https://forum.portfolio-performance.info/c/how-to", //$NON-NLS-1$
                        Messages.IntroOpenHowtos, Messages.IntroOpenHowtosText);
        addLink(section, "https://forum.portfolio-performance.info/c/faq", //$NON-NLS-1$
                        Messages.IntroOpenFAQ, Messages.IntroOpenFAQText);
    }

    private void addSectionLabel(Composite actions, String label)
    {
        Label l = new Label(actions, SWT.NONE);
        l.setText(label);
        l.setData(UIConstants.CSS.CLASS_NAME, UIConstants.CSS.HEADING1);
        GridDataFactory.fillDefaults().indent(0, 20).applyTo(l);
    }

    private void addLink(Composite container, final String target, String label, String subtext)
    {
        Link link = new Link(container, SWT.UNDERLINE_LINK);
        link.setText("<a>" + label + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        link.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                linkActivated(target);
            }
        });

        if (subtext != null)
        {
            Label l = new Label(container, SWT.WRAP);
            l.setText(subtext);
            GridDataFactory.fillDefaults().indent(0, -3).applyTo(l);
        }
    }

    public void linkActivated(String target)
    {
        if (target.startsWith(OPEN))
        {
            String file = target.substring(OPEN.length());

            // check if file is already opened somewhere

            java.util.Optional<MPart> part = partService.getParts().stream()
                            .filter(p -> UIConstants.Part.PORTFOLIO.equals(p.getElementId())) //
                            .filter(p -> file.equals(p.getPersistedState().get(UIConstants.PersistedState.FILENAME)))
                            .findAny();

            if (part.isPresent())
                partService.activate(part.get());
            else
                executeCommand(UIConstants.Command.OPEN_RECENT_FILE, UIConstants.Parameter.FILE, file);
        }
        else if ("action:open".equals(target)) //$NON-NLS-1$
        {
            executeCommand("name.abuchen.portfolio.ui.command.open"); //$NON-NLS-1$
        }
        else if ("action:new".equals(target)) //$NON-NLS-1$
        {
            executeCommand("name.abuchen.portfolio.ui.command.newclient"); //$NON-NLS-1$
        }
        else if ("action:sample".equals(target)) //$NON-NLS-1$
        {
            executeCommand("name.abuchen.portfolio.ui.command.openSample", //$NON-NLS-1$
                            UIConstants.Parameter.SAMPLE_FILE, //
                            "/" + getClass().getPackage().getName().replace('.', '/') + "/kommer.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if ("action:daxsample".equals(target)) //$NON-NLS-1$
        {
            executeCommand("name.abuchen.portfolio.ui.command.openSample", //$NON-NLS-1$
                            UIConstants.Parameter.SAMPLE_FILE, //
                            "/" + getClass().getPackage().getName().replace('.', '/') + "/dax.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        else if (target.startsWith(OPEN_PREFERENCES))
        {
            String page = target.substring(OPEN_PREFERENCES.length());
            executeCommand(UIConstants.Command.PREFERENCES, UIConstants.Parameter.PAGE, page);
        }
        else if (target.startsWith("http")) //$NON-NLS-1$
        {
            DesktopAPI.browse(target);
        }
    }

    private void executeCommand(String command, String... parameters)
    {
        try
        {
            Command cmd = commandService.getCommand(command);

            List<Parameterization> parameterizations = new ArrayList<>();
            if (parameters != null)
            {
                for (int ii = 0; ii < parameters.length; ii = ii + 2)
                {
                    IParameter p = cmd.getParameter(parameters[ii]);
                    parameterizations.add(new Parameterization(p, parameters[ii + 1]));
                }
            }

            ParameterizedCommand pCmd = new ParameterizedCommand(cmd,
                            parameterizations.toArray(new Parameterization[0]));
            if (handlerService.canExecute(pCmd))
                handlerService.executeHandler(pCmd);
        }
        catch (NotDefinedException e)
        {
            PortfolioPlugin.log(e);
            MessageDialog.openError(Display.getDefault().getActiveShell(), Messages.LabelError, e.getMessage());
        }
    }

    @Inject
    @Optional
    public void subscribeFileTopic(@UIEventTopic(UIConstants.Event.File.ALL_SUB_TOPICS) String file)
    {
        if (recentFilesComposite == null)
            return;

        Composite parent = recentFilesComposite.getParent();
        recentFilesComposite.dispose();
        createRecentFilesComposite(parent);
        recentFilesComposite.getParent().layout(true);
    }
}
