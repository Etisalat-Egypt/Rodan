/*
 * Etisalat Egypt, Open Source
 * Copyright 2021, Etisalat Egypt and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

/**
 * @author Ayman ElSherif
 */

package com.rodan.intruder.main.cli.menu;

import com.rodan.intruder.diameter.usecases.attacks.dos.*;
import com.rodan.intruder.diameter.usecases.attacks.fraud.FraudAccessRestrictionModule;
import com.rodan.intruder.diameter.usecases.attacks.fraud.FraudOdbModule;
import com.rodan.intruder.diameter.usecases.attacks.infogathering.*;
import com.rodan.intruder.diameter.usecases.attacks.location.LocationIdrModule;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleConstants;
import com.rodan.intruder.diameter.usecases.model.DiameterModuleOptions;
import com.rodan.intruder.diameter.usecases.port.DiameterGatewayFactory;
import com.rodan.intruder.main.cli.Cli;
import com.rodan.intruder.main.cli.command.Command;
import com.rodan.intruder.main.cli.command.processor.CliCommandProcessor;
import com.rodan.intruder.main.cli.executor.SyncModuleExecutor;
import com.rodan.intruder.main.cli.util.ConsoleColors;
import com.rodan.intruder.ss7.usecases.attacks.dos.DosCallBarringModule;
import com.rodan.intruder.ss7.usecases.attacks.dos.DosClModule;
import com.rodan.intruder.ss7.usecases.attacks.dos.DosDsdModule;
import com.rodan.intruder.ss7.usecases.attacks.dos.DosPurgeModule;
import com.rodan.intruder.ss7.usecases.attacks.fraud.SmsFraudModule;
import com.rodan.intruder.ss7.usecases.attacks.infogathering.*;
import com.rodan.intruder.ss7.usecases.attacks.interception.MoCallInterceptionModule;
import com.rodan.intruder.ss7.usecases.attacks.interception.MoCallInterceptionMsrnModule;
import com.rodan.intruder.ss7.usecases.attacks.interception.SmsInterceptionModule;
import com.rodan.intruder.ss7.usecases.attacks.location.LocationAtiModule;
import com.rodan.intruder.ss7.usecases.attacks.location.LocationPsiModule;
import com.rodan.intruder.ss7.usecases.attacks.location.LocationPslModule;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptionsFactoryImpl;
import com.rodan.intruder.kernel.usecases.SignalingModule;
import com.rodan.intruder.kernel.usecases.SignalingProtocol;
import com.rodan.intruder.kernel.usecases.model.ModuleOptions;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleConstants;
import com.rodan.intruder.ss7.usecases.model.Ss7ModuleOptions;
import com.rodan.intruder.ss7.usecases.port.Ss7GatewayFactory;
import com.rodan.library.model.Constants;
import com.rodan.library.model.annotation.Module;
import com.rodan.library.model.config.AbstractConfig;
import com.rodan.library.model.config.node.config.IntruderNodeConfig;
import com.rodan.library.model.error.ErrorCode;
import com.rodan.library.model.error.SystemException;
import com.rodan.library.model.error.ValidationException;
import com.rodan.library.util.Util;
import lombok.Builder;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class ModuleMenu extends MenuTemplate {
    private Class moduleClass;
    public static final List<Class<?>> SS7_MODULES = Util.getAvailableModules("com.rodan.intruder.ss7.usecases.attacks");
    public static final List<Class<?>> DIAMETER_MODULES = Util.getAvailableModules("com.rodan.intruder.diameter.usecases.attacks");

    final static Logger logger = LogManager.getLogger(ModuleMenu.class);

    @Builder
    public ModuleMenu(Map<String, String> menuParams, Ss7ModuleOptions moduleOptions,
                      IntruderNodeConfig nodeConfig, Ss7GatewayFactory ss7GatewayFactory, DiameterGatewayFactory diameterGatewayFactory) {
        super(menuParams, moduleOptions, Objects.requireNonNull(nodeConfig, "nodeConfig cannot be null"),
                ss7GatewayFactory, diameterGatewayFactory);
        var globalCommandProcessor = new GlobalCommandProcessor(null);
        this.commandProcessor = new ModuleCommandProcessor(globalCommandProcessor);
    }

    @Override
    public void validateParameters() throws ValidationException {
        boolean validModule = false;
        var moduleName = Objects.requireNonNullElse(this.getParam(MODULE_NAME_PARAM), "");
        var availableModules = Stream.concat(SS7_MODULES.stream(), DIAMETER_MODULES.stream()).toList();
        for (Class module : availableModules) {
            if (((Module) module.getAnnotation(Module.class)).name().equals(moduleName)) {
                moduleClass = module;
                validModule = true;
                break;
            }
        }

        if (!validModule) {
            String msg = "Invalid module name: [" + moduleName + "]";
            throw ValidationException.builder()
                    .code(ErrorCode.INVALID_MENU_PARAMETERS).message(msg).build();
        }
    }

    @Override
    public void loadAvailableOptions() throws ValidationException, SystemException {
        var moduleName = ((Module) moduleClass.getAnnotation(Module.class)).name();
        var protocol = ModuleOptions.getProtocol(moduleName);
        var ss7Factory = new Ss7ModuleOptionsFactoryImpl();
        ModuleOptions options = switch (protocol) {
            case SS7 -> ss7Factory.create(moduleName, getNodeConfig());
            case DIAMETER -> DiameterModuleOptions.create(moduleName, getNodeConfig());
        };

        this.setModuleOptions(options);
    }

    @Override
    protected String getCursorText() {
        String moduleName = this.getParam(MODULE_NAME_PARAM);
        return ConsoleColors.underline(Constants.APP_NAME) + " module(" + ConsoleColors.redBold(moduleName) + ")" + " > ";
    }

    protected void displayOptions(String moduleName) throws SystemException {
        // Module Options
        System.out.printf(ConsoleColors.bold("\nModule options (%s):\n\n"), moduleName);
        System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, "Name", "Current Setting", "Required", "Description");
        System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, "----", "---------------", "--------", "-----------");

        AbstractConfig options = getModuleOptions();
        var directFields = options.getClass().getDeclaredFields();
        var parentFields = options.getClass().getSuperclass().getDeclaredFields();
        var fields = Stream.concat(Arrays.stream(directFields), Arrays.stream(parentFields)).filter(AbstractConfig::isOptionField).toArray(Field[]::new);
        for (Field field : fields) {
            displayConfigEntry(options, field, false);
        }

        // Bypass Payload Options
        // TODO SS7 IMP: Implement bypass
//        options = getBypassOptions();
//        if (options != null) {
//            System.out.printf(ConsoleColors.bold("\n\nBypass options (%s):\n\n"), ((Ss7PayloadOptions) options).getPayloadName());
//            System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, "Name", "Current Setting", "Required", "Description");
//            System.out.printf("%s%-20s  %-15s  %-10s  %-15s\n", Cli.PREFIX_INDENTATION, "----", "---------------", "--------", "-----------");
//
//            directFields = options.getClass().getDeclaredFields();
//            parentFields = options.getClass().getSuperclass().getDeclaredFields();
//            fields = Stream.concat(Arrays.stream(directFields), Arrays.stream(parentFields)).toArray(Field[]::new);
//            for (Field field : fields) {
//                displayConfigEntry(options, field, true);
//            }
//        }

        System.out.print("\n");
    }

    protected void displayPayloads(String moduleName) {
        System.out.print(ConsoleColors.bold("\nCompatible Bypass Payloads:\n\n"));

        var availableModules = Stream.concat(SS7_MODULES.stream(), DIAMETER_MODULES.stream()).toList();
        for(Class module : availableModules) {
            var name = ((Module) module.getAnnotation(Module.class)).name();
            if (name.equals(moduleName)) {
                var payloads = ((Module) module.getAnnotation(Module.class)).compatiblePayloads();
                if (payloads != null && payloads.length > 0) {
                    System.out.printf("%s %-4s %-10s\n", Cli.PREFIX_INDENTATION, "#", "Name");
                    System.out.printf("%s %-4s %-10s\n", Cli.PREFIX_INDENTATION, "-", "----");
                    int index = 1;
                    for (var payload : payloads) {
                        System.out.printf("%s %-4s %-10s\n", Cli.PREFIX_INDENTATION, index, payload);
                        index++;
                    }

                } else {
                    System.out.printf("%s %s\n", Cli.PREFIX_INDENTATION, ConsoleColors.italic("No compatible payloads"));
                }
            }
        }
        System.out.print("\n");
    }

    @Override
    public void displayHelp() {
        String[] availableCommands = {"run", "list", "info", "show options", "set", "use", "use payload", "help", "back", "exit"};
        System.out.print("\nAvailable Commands:\n");
        System.out.print("==================\n");
        for (var command : availableCommands) {
            System.out.print(Cli.PREFIX_INDENTATION + command + "\n");
        }
        System.out.print("\n");
    }

    private void runModule(String moduleName) {
        try {
            System.out.printf("\n%s\n", ConsoleColors.yellow("Running module..."));
            var nodeConfig = getNodeConfig();
            SignalingModule module = null;
            var protocol = ModuleOptions.getProtocol(moduleName);
            if (protocol.equals(SignalingProtocol.SS7)) {
                var moduleOps = (Ss7ModuleOptions) getModuleOptions();
                var ss7Gateway = getSs7GatewayFactory().makeGateway(nodeConfig);
                module = switch (moduleName) {
                    // Information Gathering
                    case Ss7ModuleConstants.ROUTING_INFO_NAME -> RoutingInfoModule.builder()
                            .moduleOptions(moduleOps)
//                            .bypassOptions(getBypassOptions()) // TODO SS7 IMP: Implement bypass
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.SMS_ROUTING_INFO_NAME -> SmsRoutingInfoModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.LCS_ROUTING_INFO_NAME -> LcsRoutingInfoModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.GPRS_ROUTING_INFO_NAME -> GprsRoutingInfoModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.SEND_IMSI_NAME -> SendImsiModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.NEW_AUTH_VECTOR_NAME -> NewAuthVectorModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.CURRENT_AUTH_VECTOR_VECTOR_NAME -> CurrentAuthVectorModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.HLR_ADDRESS_SM_NAME -> HlrAddressSmModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.CAMEL_INFO_NAME -> CamelInfoModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.VLR_BF_NAME -> VlrBruteforceModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();

                    // Location Tracking
                    case Ss7ModuleConstants.LOCATION_ATI_NAME -> LocationAtiModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.LOCATION_PSI_NAME -> LocationPsiModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.LOCATION_PSL_NAME -> LocationPslModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();

                    // Call and SMS Interception
                    case Ss7ModuleConstants.SMS_INTERCEPTION_NAME -> SmsInterceptionModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.MO_CALL_INTERCEPTION_NAME -> MoCallInterceptionModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.MO_CALL_INTERCEPTION_MSRN_NAME -> MoCallInterceptionMsrnModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();

                    // Fraud
                    case Ss7ModuleConstants.SMS_FRAUD_NAME -> SmsFraudModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();

                    // Denial of Service
                    case Ss7ModuleConstants.DOS_CL_NAME -> DosClModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.DOS_DSD_NAME -> DosDsdModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.DOS_PURGE_NAME -> DosPurgeModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    case Ss7ModuleConstants.DOS_CALL_BARRING_NAME -> DosCallBarringModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(ss7Gateway)
                            .build();
                    default -> {
                        String msg = "Invalid SS7 module name: " + moduleName;
                        logger.error(msg);
                        throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
                    }
                };

            } else if (protocol.equals(SignalingProtocol.DIAMETER)) {
                var moduleOps = (DiameterModuleOptions) getModuleOptions();
                var diameterConnection = getDiameterGatewayFactory().makeGateway(nodeConfig);
                module = switch (moduleName) {
                    // Information Gathering
                    case DiameterModuleConstants.SUBSCRIBER_INFO_NAME -> SubscriberInfoModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.NEW_AUTH_PARAM_NAME -> NewAuthParameterModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.HSS_ADDRESS_AIR_NAME -> HssAddressAirModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.MME_IDR_BF_NAME -> MmeIdrBruteforceModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.MME_IDR_DISCOVERY_NAME -> MmeIdrDiscoveryModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();

                    // Location Tracking
                    case DiameterModuleConstants.LOCATION_IDR_NAME -> LocationIdrModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();

                    // Denial of Service
                    case DiameterModuleConstants.DOS_ALL_ULR_NAME -> DosAllUlrModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.DOS_MT_SMS_NAME -> DosMtSmsModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.DOS_MO_ALL_NAME -> DosMoAllModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.DOS_MO_ALL_RAT_NAME -> DosMoAllRatModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.DOS_MT_ALL_NAME -> DosMtAllModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.DOS_MT_ALL_CLR_NAME -> DosMtAllClrModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();

                    // Fraud
                    case DiameterModuleConstants.FRAUD_ODB_NAME -> FraudOdbModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();
                    case DiameterModuleConstants.FRAUD_ACCESS_RESTRICTION_NAME -> FraudAccessRestrictionModule.builder()
                            .moduleOptions(moduleOps)
                            .gateway(diameterConnection)
                            .build();

                    default -> {
                        String msg = "Invalid Diameter module name: " + moduleName;
                        logger.error(msg);
                        throw SystemException.builder().code(ErrorCode.MODULE_REQUEST_ERROR).message(msg).build();
                    }
                };
            }

            module.addNotificationListener(MenuTemplate::displayNotification);

            var executor = new SyncModuleExecutor();
            var result = executor.execute(module);
            var response = result.get();
            displaySuccess("Module output: " + response);

        } catch (Exception e) {
            var msg = e.getMessage();
            if (e instanceof ExecutionException) {
                msg = e.getCause().getMessage();
            }
            logger.error(msg, e);
            displayError(msg);
        }
    }

    private void displayInfo(String moduleName) throws SystemException {
        if (StringUtils.isNotBlank(moduleName)) {
            var availableModules = Stream.concat(SS7_MODULES.stream(), DIAMETER_MODULES.stream()).toList();
            for (Class module : availableModules) {
                var name = ((Module) module.getAnnotation(Module.class)).name();
                var displayName = ((Module) module.getAnnotation(Module.class)).displayName();
                var description = ((Module) module.getAnnotation(Module.class)).description();
                var category = ((Module) module.getAnnotation(Module.class)).category();
                var rank = ((Module) module.getAnnotation(Module.class)).rank();
                if (moduleName.equals(name)) {
                    System.out.printf("\n%s %s\n", ConsoleColors.bold("Module:"), name);
                    System.out.printf("\n%s %s\n", ConsoleColors.bold("Name:"), displayName);
                    System.out.printf("\n%s %s\n", ConsoleColors.bold("Category:"), category);
                    System.out.printf("\n%s %s\n", ConsoleColors.bold("Rank:"), rank);
                    // TODO display supported MAP versions
                    System.out.print(ConsoleColors.bold("\nDescription:\n"));
                    System.out.printf("%s\n", description);
                    break;
                }
            }
        }

        displayOptions(moduleName);
        displayPayloads(moduleName);
    }

    protected class ModuleCommandProcessor extends CliCommandProcessor {
        public ModuleCommandProcessor(CliCommandProcessor next) {
            super(next);
        }

        public boolean handle(Command command) throws SystemException {
            boolean commandHandled = false;
            var moduleName = Objects.requireNonNullElse(getParam(MODULE_NAME_PARAM), "");

            switch (command.getCommandText()) {
                case SHOW_OPTIONS:
                    displayOptions(moduleName);
                    commandHandled = true;
                    break;

                case SHOW_BYPASS_PAYLOADS:
                    displayPayloads(moduleName);
                    commandHandled = true;
                    break;

                case SET:
                    var param = command.getParams().entrySet().iterator().next();
                    var key = param.getKey();
                    var value = param.getValue();
                    AbstractConfig options = getModuleOptions();
                    commandHandled = options.trySetFieldByName(key, value);
                    // TODO IMP remove below and load bypass options from FakeSubscriberInfo
//                    if (!commandHandled) {
//                        if (key.startsWith("b_")) {
//                            key = key.substring(2);
//                            options = getBypassOptions();
//                        } else {
//                            options = getPayloadOptions();
//                        }
//                        commandHandled = options.trySetFieldByName(key, value);
//                    }
                    break;

                // TODO SS7 IMP: Implement bypass
//                case SET_BYPASS:
//                    for (Map.Entry<String, String> entry : command.getParams().entrySet()) {
//                        var payloadName = entry.getValue();
//                        var bypassOptions = Ss7PayloadOptions.create(payloadName, getNodeConfig());
//                        if (bypassOptions != null) {
//                            setBypassOptions(bypassOptions);
//                            commandHandled = true;
//                        } else {
//                            commandHandled = false;
//                        }
//                    }
//                    break;

                case UNSET:
                    param = command.getParams().entrySet().iterator().next();
                    key = param.getKey();
                    value = "";
                    options = getModuleOptions();
                    commandHandled = options.trySetFieldByName(key, value);
                    break;

                case RUN:
                    runModule(moduleName);
                    commandHandled = true;
                    break;

                case INFO:
                    displayInfo(moduleName);
                    commandHandled = true;
                    break;
            }

            return commandHandled;
        }
    }
}
