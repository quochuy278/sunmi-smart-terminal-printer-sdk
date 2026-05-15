import React, { useState } from "react";
import {
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  View
} from "react-native";
import {
  cutPaper,
  getDeviceInfo,
  getPrinterStatus,
  prepare,
  print,
  printText
} from "./native";
import {
  type SunmiDeviceInfo,
  type SunmiPrinterStatus
} from "./types";
import { resolveSunmiPrinterError } from "./errors";
import { buildSunmiSmokeTestJob } from "./smokeTestJob";

type SmokeTestResult = {
  label: string;
  payload: string;
};

const formatValue = (value: unknown): string => {
  if (typeof value === "string") {
    return value;
  }

  try {
    return JSON.stringify(value, null, 2);
  } catch {
    return String(value);
  }
};

type ActionButtonProps = {
  label: string;
  onPress: () => Promise<void>;
};

const ActionButton = ({ label, onPress }: ActionButtonProps): React.JSX.Element => {
  const [busy, setBusy] = useState(false);

  const handlePress = async (): Promise<void> => {
    if (busy) {
      return;
    }

    setBusy(true);
    try {
      await onPress();
    } finally {
      setBusy(false);
    }
  };

  return (
    <Pressable
      accessibilityRole="button"
      disabled={busy}
      onPress={() => {
        void handlePress();
      }}
      style={({ pressed }) => [
        styles.button,
        busy ? styles.buttonDisabled : null,
        pressed ? styles.buttonPressed : null
      ]}
    >
      <Text style={styles.buttonLabel}>{busy ? `${label}...` : label}</Text>
    </Pressable>
  );
};

export const SunmiPrinterSmokeTestScreen = (): React.JSX.Element => {
  const [results, setResults] = useState<SmokeTestResult[]>([]);

  const pushResult = (label: string, payload: unknown): void => {
    setResults(current => [
      {
        label,
        payload: formatValue(payload)
      },
      ...current
    ]);
  };

  const runAction = async <T,>(
    label: string,
    action: () => Promise<T>
  ): Promise<T | null> => {
    try {
      const result = await action();
      pushResult(label, result);
      return result;
    } catch (error) {
      pushResult(`${label} error`, resolveSunmiPrinterError(error));
      return null;
    }
  };

  const handleDeviceInfo = async (): Promise<void> => {
    await runAction<SunmiDeviceInfo>("getDeviceInfo()", getDeviceInfo);
  };

  const handlePrepare = async (): Promise<void> => {
    await runAction<boolean>("prepare()", prepare);
  };

  const handleStatus = async (): Promise<void> => {
    await runAction<SunmiPrinterStatus>("getPrinterStatus()", getPrinterStatus);
  };

  const handlePrintText = async (): Promise<void> => {
    await runAction<boolean>("printText()", async () =>
      printText("Hello from SUNMI P2 SE\n", {
        align: "center",
        bold: true,
        fontSize: 24
      })
    );
  };

  const handlePrintJob = async (): Promise<void> => {
    await runAction<boolean>("print(job)", async () =>
      print(buildSunmiSmokeTestJob())
    );
  };

  const handleCut = async (): Promise<void> => {
    await runAction<boolean>("cutPaper()", cutPaper);
  };

  return (
    <ScrollView contentContainerStyle={styles.container}>
      <Text style={styles.title}>SUNMI printer smoke test</Text>
      <Text style={styles.subtitle}>
        Run this on a SUNMI P2 SE host app to verify bind, status, text print, receipt print, and cut behavior.
      </Text>

      <View style={styles.buttonGroup}>
        <ActionButton label="Get device info" onPress={handleDeviceInfo} />
        <ActionButton label="Prepare printer" onPress={handlePrepare} />
        <ActionButton label="Get printer status" onPress={handleStatus} />
        <ActionButton label="Print text" onPress={handlePrintText} />
        <ActionButton label="Print sample receipt" onPress={handlePrintJob} />
        <ActionButton label="Cut paper" onPress={handleCut} />
      </View>

      <Text style={styles.logTitle}>Results</Text>
      <View style={styles.logList}>
        {results.length === 0 ? (
          <Text style={styles.emptyState}>No actions run yet.</Text>
        ) : null}
        {results.map((result, index) => (
          <View key={`${result.label}-${index}`} style={styles.logCard}>
            <Text style={styles.logLabel}>{result.label}</Text>
            <Text style={styles.logPayload}>{result.payload}</Text>
          </View>
        ))}
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    padding: 16,
    gap: 16
  },
  title: {
    fontSize: 24,
    fontWeight: "700",
    color: "#101828"
  },
  subtitle: {
    fontSize: 14,
    lineHeight: 20,
    color: "#475467"
  },
  buttonGroup: {
    gap: 12
  },
  button: {
    backgroundColor: "#111827",
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 14
  },
  buttonPressed: {
    opacity: 0.85
  },
  buttonDisabled: {
    backgroundColor: "#9CA3AF"
  },
  buttonLabel: {
    color: "#FFFFFF",
    fontSize: 16,
    fontWeight: "600"
  },
  logTitle: {
    fontSize: 18,
    fontWeight: "700",
    color: "#101828"
  },
  logList: {
    gap: 12
  },
  emptyState: {
    color: "#667085",
    fontSize: 14
  },
  logCard: {
    borderWidth: 1,
    borderColor: "#D0D5DD",
    borderRadius: 12,
    backgroundColor: "#F8FAFC",
    padding: 12,
    gap: 8
  },
  logLabel: {
    fontSize: 14,
    fontWeight: "700",
    color: "#101828"
  },
  logPayload: {
    fontSize: 12,
    lineHeight: 18,
    color: "#344054",
    fontFamily: "Courier"
  }
});
