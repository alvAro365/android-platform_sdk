/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.eclipse.org/org/documents/epl-v10.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/

package com.android.ide.eclipse.adt.internal.sourcelookup;

import com.android.ide.eclipse.adt.internal.project.AndroidClasspathContainerInitializer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DefaultSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ExternalArchiveSourceContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.JavaSourceLookupDirector;

import java.io.File;

public class AdtSourceLookupDirector extends JavaSourceLookupDirector {

    @Override
    public void initializeDefaults(ILaunchConfiguration configuration) throws CoreException {
        dispose();
        setLaunchConfiguration(configuration);
        String projectName = configuration.getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", //$NON-NLS-1$
                ""); //$NON-NLS-1$
        if (projectName != null && projectName.length() > 0) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            if (project != null && project.isOpen()) {
                IJavaProject javaProject = JavaCore.create(project);
                if (javaProject != null && javaProject.isOpen()) {
                    IClasspathEntry[] entries = javaProject.getRawClasspath();
                    IClasspathEntry androidEntry = null;
                    for (int i = 0; i < entries.length; i++) {
                        IClasspathEntry entry = entries[i];
                        if (entry.getPath() != null
                                && AndroidClasspathContainerInitializer.CONTAINER_ID.equals(entry
                                        .getPath().toString())) {
                            androidEntry = entry;
                            break;
                        }
                    }
                    if (androidEntry != null) {
                        IPath sourceAttachmentPath = androidEntry.getSourceAttachmentPath();
                        if (sourceAttachmentPath != null) {
                            String androidSrc = sourceAttachmentPath.toString();
                            if (androidSrc != null && androidSrc.trim().length() > 0) {
                                File srcFile = new File(androidSrc);
                                ISourceContainer adtContainer = null;
                                if (srcFile.isFile()) {
                                    adtContainer = new ExternalArchiveSourceContainer(androidSrc,
                                            true);
                                }
                                if (srcFile.isDirectory()) {
                                    adtContainer = new DirectorySourceContainer(srcFile, false);
                                }
                                if (adtContainer != null) {
                                    ISourceContainer defaultContainer =
                                        new DefaultSourceContainer();
                                    setSourceContainers(new ISourceContainer[] {
                                            adtContainer, defaultContainer
                                    });
                                    initializeParticipants();
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        }
        setSourceContainers(new ISourceContainer[] {
            new DefaultSourceContainer()
        });
        initializeParticipants();
    }

}
