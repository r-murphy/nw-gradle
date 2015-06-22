package rm.nw.gradle

/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import javax.inject.Inject

import org.gradle.api.internal.file.FileResolver
import org.gradle.internal.reflect.Instantiator
import org.gradle.plugins.ear.EarPluginConvention
import org.gradle.util.ConfigureUtil

import rm.nw.gradle.descriptor.SAPManifest


public class NWEarPluginConvention extends EarPluginConvention  {
	
	/**
	 * A custom SAP Manifest configuration. Default is an "SAP_MANIFEST.MF" with sensible defaults.
	 */
	SAPManifest sapManifest
	
	//these are private in NWEarPluginConvention so need to save them ourselves
	private final FileResolver fileResolver
	private final Instantiator instantiator
	
	@Inject
	public NWEarPluginConvention(FileResolver fileResolver, Instantiator instantiator) {
		super(fileResolver, instantiator)
		this.instantiator = instantiator;
		this.fileResolver = fileResolver;
		this.sapManifest {}
	}
	
	/**
	 * Configures the SAP Manifest for this EAR archive.
	 * <p>The given closure is executed to configure the manifest. The {@link SAPManifest}
	 * is passed to the closure as its delegate.</p>
	 * 
	 * @param configureClosure
	 * @return This.
	 */
	public NWEarPluginConvention sapManifest(Closure configureClosure) {
//		println('!!!!!!!!!!!!NWEarPluginConvention.sapManifest Closure');
		if (!sapManifest) {
//            println('No sapManifest. creating one');
    
//			if (project!=null) {
//				sapManifest = instantiator.newInstance(SAPManifest.class, fileResolver, project);
//			}
//			else {
				sapManifest = instantiator.newInstance(SAPManifest.class, fileResolver);
//                println('Created sapManifest:' + sapManifest);
//			}
		}
		ConfigureUtil.configure(configureClosure, sapManifest)
		return this
	}
	
}