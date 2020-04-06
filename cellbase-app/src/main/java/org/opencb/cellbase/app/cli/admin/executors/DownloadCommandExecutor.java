/*
 * Copyright 2015-2020 OpenCB
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

package org.opencb.cellbase.app.cli.admin.executors;

import com.beust.jcommander.ParameterException;
import org.apache.commons.lang3.StringUtils;
import org.opencb.cellbase.app.cli.CommandExecutor;
import org.opencb.cellbase.app.cli.admin.AdminCliOptionsParser;
import org.opencb.cellbase.core.config.SpeciesConfiguration;
import org.opencb.cellbase.core.exception.CellbaseException;
import org.opencb.cellbase.lib.EtlCommons;
import org.opencb.cellbase.lib.SpeciesUtils;
import org.opencb.cellbase.lib.download.ClinicalDownloadManager;
import org.opencb.cellbase.lib.download.CoreDownloadManager;
import org.opencb.cellbase.lib.download.DownloadManager;
import org.opencb.cellbase.lib.download.RegulationDownloadManager;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 03/02/15.
 */
public class DownloadCommandExecutor extends CommandExecutor {

    private AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions;
    private Path outputDirectory;

    public DownloadCommandExecutor(AdminCliOptionsParser.DownloadCommandOptions downloadCommandOptions) {
        super(downloadCommandOptions.commonOptions.logLevel, downloadCommandOptions.commonOptions.conf);

        this.downloadCommandOptions = downloadCommandOptions;
        this.outputDirectory = Paths.get(downloadCommandOptions.outputDirectory);
    }

    /**
     * Execute specific 'download' command options.
     */
    public void execute() {
        try {
//            SpeciesConfiguration speciesConfiguration = SpeciesUtils.getSpeciesConfiguration(configuration,
//                    downloadCommandOptions.speciesAndAssemblyOptions.species);
//            if (speciesConfiguration == null) {
//                throw new CellbaseException("Invalid species: '" + downloadCommandOptions.speciesAndAssemblyOptions.species + "'");
//            }
//            SpeciesConfiguration.Assembly assembly = null;
//            if (!StringUtils.isEmpty(downloadCommandOptions.speciesAndAssemblyOptions.assembly)) {
//                assembly = SpeciesUtils.getAssembly(speciesConfiguration, downloadCommandOptions.speciesAndAssemblyOptions.assembly);
//                if (assembly == null) {
//                    throw new CellbaseException("Invalid assembly: '" + downloadCommandOptions.speciesAndAssemblyOptions.assembly + "'");
//                }
//            } else {
//                assembly = SpeciesUtils.getDefaultAssembly(speciesConfiguration);
//            }

            String species = downloadCommandOptions.speciesAndAssemblyOptions.species;
            String assembly = downloadCommandOptions.speciesAndAssemblyOptions.assembly;
//            logger.info("Processing species " + speciesConfiguration.getScientificName());

            List<String> dataList = getDataList(species);
            DownloadManager downloadManager = new DownloadManager(species, assembly, outputDirectory, configuration);
            for (String data : dataList) {
                switch (data) {
                    case EtlCommons.GENOME_DATA:
                        new CoreDownloadManager(species, assembly, outputDirectory, configuration).downloadReferenceGenome();
                        break;
                    case EtlCommons.GENE_DATA:
                        new CoreDownloadManager(species, assembly, outputDirectory, configuration).downloadEnsemblGene();
                        if (!dataList.contains(EtlCommons.GENOME_DATA)) {
                            // user didn't specify genome data to download, but we need it for gene data sources
                            new CoreDownloadManager(species, assembly, outputDirectory, configuration).downloadReferenceGenome();
                        }
                        break;
//                    case EtlCommons.VARIATION_DATA:
//                        downloadManager.downloadVariation();
//                        break;
                    case EtlCommons.VARIATION_FUNCTIONAL_SCORE_DATA:
                        downloadManager.downloadCaddScores();
                        break;
                    case EtlCommons.REGULATION_DATA:
                        new RegulationDownloadManager(species, assembly, outputDirectory, configuration).downloadRegulation();
                        break;
                    case EtlCommons.PROTEIN_DATA:
                        new CoreDownloadManager(species, assembly, outputDirectory, configuration).downloadProtein();
                        break;
                    case EtlCommons.CONSERVATION_DATA:
                        new CoreDownloadManager(species, assembly, outputDirectory, configuration).downloadConservation();
                        break;
                    case EtlCommons.CLINICAL_VARIANTS_DATA:
                        new ClinicalDownloadManager(species, assembly, outputDirectory, configuration).downloadClinical();
                        break;
                    case EtlCommons.STRUCTURAL_VARIANTS_DATA:
                        downloadManager.downloadStructuralVariants();
                        break;
                    case EtlCommons.REPEATS_DATA:
                        downloadManager.downloadRepeats();
                        break;
                    case EtlCommons.OBO_DATA:
                        new CoreDownloadManager(species, assembly, outputDirectory, configuration).downloadObo();
                        break;
                    default:
                        System.out.println("Value \"" + data + "\" is not allowed for the data parameter. Allowed values"
                                + " are: {genome, gene, gene_disease_association, variation, variation_functional_score,"
                                + " regulation, protein, conservation, clinical_variants}");
                        break;
                }
            }
            downloadManager.writeDownloadLogFile();
        } catch (ParameterException e) {
            logger.error("Error in 'download' command line: " + e.getMessage());
        } catch (IOException | InterruptedException | CellbaseException e) {
            logger.error("Error downloading '" + downloadCommandOptions.speciesAndAssemblyOptions.species + "' files: " + e.getMessage());
        }

    }

    private List<String> getDataList(String species) throws CellbaseException {
        if (StringUtils.isEmpty(downloadCommandOptions.data) || downloadCommandOptions.data.equals("all")) {
            return SpeciesUtils.getSpeciesConfiguration(configuration, species).getData();
        } else {
            return Arrays.asList(downloadCommandOptions.data.split(","));
        }
    }

    @Deprecated
    private List<String> getDataList(SpeciesConfiguration sp) {
        List<String> dataList;
        if (downloadCommandOptions.data.equals("all")) {
            dataList = sp.getData();
        } else {
            dataList = Arrays.asList(downloadCommandOptions.data.split(","));
        }
        return dataList;
    }
}
