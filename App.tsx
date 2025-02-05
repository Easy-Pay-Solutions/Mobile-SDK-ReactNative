/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */
//https://dev.to/kylefoo/xcode-12-new-build-system-warns-multiple-commands-produce-assets-car-56im
//RCT_NEW_ARCH_ENABLED=0 pod install // https://github.com/facebook/react-native/issues/34723
import React, { useEffect } from 'react';
import type { PropsWithChildren } from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  useColorScheme,
  View,
  Alert,
  NativeModules,
  NativeEventEmitter,
  Platform
} from 'react-native';

import {
  Colors,
  Header,
} from 'react-native/Libraries/NewAppScreen';

const { EasyPayModule } = NativeModules;
const nativeEventEmitter = new NativeEventEmitter(NativeModules.RNEventEmitter);

function App(): React.JSX.Element {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  useEffect(() => {
    if (Platform.OS === 'ios') {
      checkNativeModuleMethods('EasyPayModule');
    }
    // iOS Events
    const selectionListener = nativeEventEmitter.addListener('onCardSelected', (data: { consentId: String; }) => {
      console.log('Card Selected', data.consentId);
    });
    const deleteListener = nativeEventEmitter.addListener('onCardDeleted', (data: { consentId: String; }) => {
      console.log('Card Deleted', data.consentId);
    });
    const saveListener = nativeEventEmitter.addListener('onCardSaved', (data: { consentId: String; }) => {
      console.log('Card Saved', data.consentId);
      /* See also:
        consentId
        expMonth
        expYear
        last4Digits
      */
    });
    const paidListener = nativeEventEmitter.addListener('onCardPaid', (data: {
      consentId: string;
      functionOk: boolean;
      txApproved: boolean;
      responseMessage: string;
      errorMessage: string;
      errorCode: int;
      txnCode: string;
      avsResult: string;
      cvvResult: string;
      acquirerResponseEMV: string;
      txId: int;
      requiresVoiceAuth: boolean;
      isPartialApproval: boolean;
      responseAuthorizedAmount: number;
      responseApprovedAmount: number;
     
     }) => {
    
      console.log('functionOK', data.functionOk);
      console.log('Consent ID', data.consentId);
      console.log('Transaction ID', data.txId);
    });
    
    // Android Events
    const manageErrorListener = nativeEventEmitter.addListener('onManageError', (data: { error: String; }) => {
      console.log('Manage error', data.error);
    });

    const manageResultListener = nativeEventEmitter.addListener('onManageResult', (data: { selectedConsentId?: number; }) => {
      console.log('Selected Consent Id', data.selectedConsentId);
       /* See also:
        addedConsents
        deletedConsents
       */
    });

    const paymentErrorListener = nativeEventEmitter.addListener('onPaymentError', (data: { error: String; }) => {
      console.log('Payment error', data.error);
    });
    const paymentCancelListener = nativeEventEmitter.addListener('onPaymentCancelled', () => {
      console.log('Payment cancelled');
    });
    const paymentResultListener = nativeEventEmitter.addListener('onPaymentResult', (result: { data: {
      errorMessage: string;
      responseMessage: string;
      txApproved: boolean;
      txId: number;
      txCode: string;
      avsResult: string;
      acquirerResponseEmv: string;
      cvvResult: string;
      isPartialApproval: boolean;
      requiresVoiceAuth: boolean;
      responseApprovedAmount: number;
      responseAuthorizedAmount: number;
      responseBalanceAmount: number;
    }; }) => {
      /* See also:
        addedConsents
        deletedConsents
       */
      console.log('Payment result', result.data);
    });

    return () => {
      selectionListener.remove();
      deleteListener.remove();
      saveListener.remove();
      paidListener.remove();

      manageErrorListener.remove();
      manageResultListener.remove();
      paymentErrorListener.remove();
      paymentCancelListener.remove();
      paymentResultListener.remove();
    }
  }, []);

  const testConfigureSecrets = () => {
    if (Platform.OS === 'ios') {
      testConfigureSecretsIOS();
    } else {
      testConfigureSecretsAndroid();
    }
  }

  const testCertificateStatus = async () => {
    EasyPayModule.getCertificateStatus((status: string) => {
      showAlert(status);
    });
  }

  const testLoadCertificate = () => {
    if (Platform.OS === 'ios') {
      EasyPayModule.loadCertificate()
    } else {
      //there is not loadCertificate method in Android, so the workaround is to call configureSecrets again
      testConfigureSecretsAndroid();
    } 
  }

  //Android Native Module methods

  const testConfigureSecretsAndroid = () => {
    if (!EasyPayModule) {
      showAlert("EasyPayModule is undefined");
      return;
    }

    EasyPayModule.configureSecrets("628E32BF0D544DDA88303238373541303338323735",
      "8E66DBB3D691C9E0FDF341E4AB38C3C9",
      null)
  }

  //iOS Native Module methods
  const testConfigureSecretsIOS = () => {
    if (!EasyPayModule) {
      showAlert("EasyPayModule is undefined");
      return;
    }

    EasyPayModule.configureSecrets("628E32BF0D544DDA88303238373541303338323735",
      "8E66DBB3D691C9E0FDF341E4AB38C3C9",
      null,
      true)
  }

  const testManageAndSelect = async () => {
    try {
      const config = {
        rpguid: "3d3424a6-c5f3-4c28",
        customerReferenceId: "12456",
        merchantId: "1"
      };
      const user = {
        endCustomerFirstName: "Simple",
        endCustomerLastName: "Simon",
      }
      const payment = {
        limitPerCharge: "1000.0",
        limitLifetime: "10000.0",
        // cardId: 123 // optional
      }
      const address = {
        endCustomerAddress1: "A1",
        endCustomerAddress2: "",
        endCustomerCity: "Newark",
        endCustomerState: "AZ",
        endCustomerZip: "90210",
      } 
      await EasyPayModule.manageAndSelect(
        config,
        user,
        payment,
        address
      )
    } catch (error) {
      console.log(error)
    }
  }

  const testManageAndPay = async () => {
    try {
      const config = {
        rpguid: "3d3424a6-c5f3-4c28",
        customerReferenceId: "12456",
        merchantId: "1"
      };
      const user = {
        endCustomerFirstName: "Simple",
        endCustomerLastName: "Simon",
      }
      const payment = {
        amount: "25.99",
        limitPerCharge: "1000.0",
        limitLifetime: "10000.0",
        // cardId: 123 // optional
      }
      const address = {
        endCustomerAddress1: "A1",
        endCustomerAddress2: "",
        endCustomerCity: "Newark",
        endCustomerState: "AZ",
        endCustomerZip: "90210",
      } 
      await EasyPayModule.pay (
        config,
        user,
        payment,
        address
      )
    } catch (error) {
      console.log(error)
    }
  }

  function showAlert(msg: string) {
    console.log("TEST " + msg);
    Alert.alert('EasyPay', msg , [
      {text: 'OK', onPress: () => console.log('OK Pressed')},
    ]);
  }

  const checkNativeModuleMethods = (moduleName: string | number) => {
    if (NativeModules[moduleName]) {
      console.log(`Methods available in ${moduleName}:`);
      console.log(Object.keys(NativeModules[moduleName]));
    } else {
      console.log(`Module ${moduleName} is not available.`);
    }
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar
        barStyle={isDarkMode ? 'light-content' : 'dark-content'}
        backgroundColor={backgroundStyle.backgroundColor}
      />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View style={[styles.container, { backgroundColor: backgroundStyle.backgroundColor}]}>
          <TouchableOpacity
            style={styles.button}
            onPress={testConfigureSecrets}>
            <Text style={styles.buttonText}>Init SDK (configureSecrets)</Text>
          </TouchableOpacity>
        </View>
        <View style={[styles.container, { backgroundColor: backgroundStyle.backgroundColor}]}>
          <TouchableOpacity
            style={styles.button}
            onPress={testCertificateStatus}>
            <Text style={styles.buttonText}>certificateStatus</Text>
          </TouchableOpacity>
        </View>
        <View style={[styles.container, { backgroundColor: backgroundStyle.backgroundColor}]}>
          <TouchableOpacity
            style={styles.button}
            onPress={testLoadCertificate}>
            <Text style={styles.buttonText}>loadCertificate</Text>
          </TouchableOpacity>
        </View>
        <View style={[styles.container, { backgroundColor: backgroundStyle.backgroundColor}]}>
          <TouchableOpacity
            style={styles.button}
            onPress={testManageAndSelect}>
            <Text style={styles.buttonText}>Select and Manage Cards</Text>
          </TouchableOpacity>
        </View>
        <View style={[styles.container, { backgroundColor: backgroundStyle.backgroundColor}]}>
          <TouchableOpacity
            style={styles.button}
            onPress={testManageAndPay}>
            <Text style={styles.buttonText}>Manage and Pay</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#ffffff',
    marginVertical: 16,
  },
  button: {
    backgroundColor: '#007bff',
    paddingVertical: 12,
    paddingHorizontal: 20,
    borderRadius: 8,
  },
  buttonText: {
    color: '#fff',
    fontSize: 18,
    textAlign: 'center',
  },
});

export default App;
