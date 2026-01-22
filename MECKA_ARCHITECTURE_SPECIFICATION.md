# Mecka Agentic Operating Intelligence - Foundational Specification

## Executive Summary

Mecka is a consent-based, audio-first, agentic operating intelligence designed to provide intelligent assistance across multiple platforms while respecting system security models and user autonomy. This specification defines the foundational architecture for Phase 0 implementation.

## 1. Agent Runtime Architecture

### 1.1 Agent Lifecycle Management

#### Agent States
```typescript
enum AgentState {
  INITIALIZING = "initializing",     // Loading dependencies, permissions
  IDLE = "idle",                     // Waiting for triggers/events
  EXECUTING = "executing",           // Active task execution
  SUSPENDED = "suspended",           // Temporarily paused (resource limits)
  TERMINATING = "terminating",       // Graceful shutdown
  TERMINATED = "terminated"          // Clean shutdown complete
}
```

#### Lifecycle Hooks
```typescript
interface AgentLifecycle {
  onInitialize(): Promise<void>;      // Setup phase
  onStart(): Promise<void>;          // Activation
  onSuspend(): Promise<void>;        // Resource pause
  onResume(): Promise<void>;          // Resource resume
  onTerminate(): Promise<void>;      // Cleanup phase
  onError(error: Error): Promise<void>; // Error handling
}
```

#### Initialization Protocol
1. **Permission Validation**: Verify all required permissions granted
2. **Dependency Resolution**: Load required modules and dependencies
3. **Resource Allocation**: Reserve memory, file handles, network ports
4. **Security Context**: Establish execution boundaries
5. **Registration**: Register with IPC bus and arbitration system

### 1.2 Scheduling & Concurrency Model

#### Hierarchical Scheduling
```typescript
interface SchedulerConfig {
  maxConcurrentAgents: number;        // Global concurrency limit
  priorityQueues: {
    critical: Agent[],                // Security, safety
    high: Agent[],                   // User-initiated, time-sensitive
    normal: Agent[],                  // Standard operations
    background: Agent[]               // Non-urgent, low-impact
  };
  timeSlices: {
    critical: number;                 // CPU time slice (ms)
    high: number;
    normal: number;
    background: number;
  };
}
```

#### Execution Context
```typescript
class ExecutionContext {
  agent: Agent;
  priority: Priority;
  deadline?: Date;                   // Real-time constraints
  resources: ResourceLimits;          // CPU, memory, network
  cancellationToken: CancellationToken;
  
  acquire(): Promise<boolean>;        // Resource acquisition
  release(): void;                   // Resource release
  suspend(): Promise<void>;           // Temporary suspension
}
```

### 1.3 Error Handling & Recovery Protocols

#### Error Classification
```typescript
enum ErrorSeverity {
  FATAL = "fatal",                   // Agent termination required
  CRITICAL = "critical",              // Immediate attention needed
  WARNING = "warning",                // Degraded functionality
  INFO = "info"                       // Informational
}

interface AgentError {
  id: string;
  agentId: string;
  severity: ErrorSeverity;
  code: string;
  message: string;
  timestamp: Date;
  context: Record<string, any>;
  recovery?: RecoveryStrategy;
}
```

#### Recovery Strategies
```typescript
interface RecoveryStrategy {
  retry?: {
    maxAttempts: number;
    backoff: "linear" | "exponential";
    delay: number;
  };
  fallback?: {
    alternative: string;              // Fallback agent/method
    timeout: number;
  };
  escalation?: {
    notify: boolean;
    priority: Priority;
  };
}
```

### 1.4 Resource Limits & Cleanup

#### Resource Management
```typescript
interface ResourceLimits {
  memory: {
    maxHeap: number;                 // MB
    maxStack: number;                 // KB
  };
  cpu: {
    maxCores: number;
    timeSlice: number;               // ms per scheduling cycle
  };
  network: {
    maxBandwidth: number;             // bytes/second
    maxConnections: number;
  };
  storage: {
    maxCache: number;                 // MB
    maxLogs: number;                 // MB
  };
}
```

## 2. IPC Protocol & State Bus Design

### 2.1 Inter-Agent Communication Protocol

#### Message Format
```typescript
interface IPCMessage {
  id: string;                        // Unique message ID
  from: string;                      // Sender agent ID
  to: string | string[];            // Recipient(s)
  type: MessageType;                 // REQUEST, RESPONSE, EVENT, BROADCAST
  priority: Priority;                // Message urgency
  payload: any;                      // Message data
  timestamp: Date;
  expires?: Date;                    // Message expiry
  correlationId?: string;            // Request-response correlation
  encryption?: EncryptionMetadata;   // Security metadata
}
```

#### Message Routing
```typescript
class MessageRouter {
  routes: Map<string, RouteConfig>;
  
  route(message: IPCMessage): Promise<DeliveryResult>;
  broadcast(message: IPCMessage, filter?: Filter): Promise<DeliveryResult[]>;
  request<T>(message: IPCMessage, timeout?: number): Promise<T>;
  response(correlationId: string, result: any): void;
}
```

### 2.2 Shared State Bus Architecture

#### Pub/Sub Architecture
```typescript
interface StateBus {
  publish(topic: string, data: any, priority?: Priority): Promise<void>;
  subscribe(topic: string, handler: StateHandler): Subscription;
  unsubscribe(topic: string, handler: StateHandler): void;
  query(topic: string, filter?: Filter): Promise<any[]>;
}

interface StateTopic {
  name: string;
  retention: "ephemeral" | "persistent" | "transactional";
  priority: Priority;
  subscribers: Set<StateHandler>;
  history?: {
    maxSize: number;
    maxAge: number;                  // milliseconds
  };
}
```

#### Priority Levels
```typescript
enum BusPriority {
  CRITICAL = 0,                      // Security, safety, immediate response
  HIGH = 1,                          // User interactions, time-sensitive
  NORMAL = 2,                        // Standard operations
  LOW = 3,                           // Background updates, analytics
  BULK = 4                           // Batch operations, non-critical
}
```

### 2.3 Message Serialization & Compression

#### Serialization Strategy
```typescript
interface SerializationConfig {
  format: "json" | "protobuf" | "msgpack" | "custom";
  compression: "none" | "gzip" | "lz4" | "zstd";
  encryption?: {
    algorithm: string;
    keyRotation: number;             // hours
  };
  versioning: {
    enabled: boolean;
    maxVersions: number;
  };
}

class MessageSerializer {
  serialize(message: IPCMessage): Buffer;
  deserialize(buffer: Buffer): IPCMessage;
  compress(buffer: Buffer): Buffer;
  decompress(buffer: Buffer): Buffer;
}
```

### 2.4 Latency Guarantees for Critical Paths

#### SLA Definitions
```typescript
interface LatencySLA {
  critical: {
    maxLatency: number;              // milliseconds
    percentile: number;              // 99.9%
    throughput: number;               // messages/second
  };
  high: {
    maxLatency: number;
    percentile: number;
    throughput: number;
  };
  normal: {
    maxLatency: number;
    percentile: number;
    throughput: number;
  };
}
```

## 3. Agent Arbitration & Conflict Resolution

### 3.1 Priority Hierarchy

#### Agent Categories & Priorities
```typescript
enum AgentCategory {
  SECURITY = 0,                      // Security monitoring, threat detection
  EXECUTOR = 1,                      // Action execution, automation
  LISTENER = 2,                      // Event monitoring, data collection
  CONTEXT = 3,                       // Context management, memory
  UI = 4,                           // User interface, feedback
  ANALYTICS = 5                      // Analytics, reporting
}
```

#### Priority Resolution Matrix
```typescript
interface PriorityMatrix {
  [higher: number]: {
    [lower: number]: ResolutionAction;
  };
}

enum ResolutionAction {
  IMMEDIATE_OVERRIDE = "immediate_override",    // Higher priority wins
  COOPERATIVE_SHARING = "cooperative_sharing", // Time-sliced execution
  QUEUE_PRIORITY = "queue_priority",           // Higher in queue
  NEGOTIATION = "negotiation",                // Agents negotiate
  USER_DECISION = "user_decision"             // User chooses
}
```

### 3.2 Deadlock Prevention Mechanisms

#### Lock Management
```typescript
class DeadlockPrevention {
  private locks: Map<string, LockInfo>;
  private waitGraph: WaitGraph;
  
  acquireLock(resource: string, agentId: string, timeout: number): Promise<boolean>;
  releaseLock(resource: string, agentId: string): void;
  detectDeadlock(): boolean;
  resolveDeadlock(strategy: "preemption" | "timeout" | "negotiation"): void;
}

interface LockInfo {
  owner: string;
  holders: Set<string>;
  waiters: Queue<{agentId: string; priority: number}>;
  timeout: number;
  acquired: Date;
}
```

#### Resource Ordering
```typescript
interface ResourceOrdering {
  globalOrder: string[];                    // Consistent lock ordering
  partialOrder: Map<string, string[]>;      // Category-specific ordering
  dynamicReorder: boolean;                  // Allow runtime reordering
}

class OrderedResourceManager {
  requestResources(resources: string[], agentId: string): Promise<boolean>;
  releaseResources(resources: string[], agentId: string): void;
  reorderResources(resources: string[]): boolean;  // Dynamic reordering
}
```

### 3.3 Intent Override Rules

#### Intent Classification
```typescript
interface Intent {
  id: string;
  agentId: string;
  type: IntentType;
  priority: number;
  resources: string[];
  duration: number;                          // Estimated duration (ms)
  conflictResolution: ConflictStrategy;
  userConfirmation?: boolean;                 // Requires user approval
}

enum IntentType {
  SYSTEM = "system",                         // Critical system operations
  USER = "user",                             // User-initiated actions
  AUTOMATED = "automated",                   // Scheduled/rule-based
  EMERGENCY = "emergency"                    // Safety/security events
}

enum ConflictStrategy {
  IMMEDIATE = "immediate",                   // No delay, override others
  SCHEDULE = "schedule",                    // Queue with scheduling
  NEGOTIATE = "negotiate",                  // Agent negotiation required
  USER_APPROVE = "user_approve"             // User decision required
}
```

### 3.4 Conflict Detection & Resolution

#### Detection Algorithms
```typescript
class ConflictDetector {
  detectResourceConflicts(intents: Intent[]): Conflict[];
  detectPriorityInversions(schedule: ExecutionPlan): Inversion[];
  detectDeadlocks(lockGraph: LockGraph): Deadlock[];
  predictConflicts(futureIntents: Intent[]): Prediction[];
}

interface Conflict {
  type: "resource" | "priority" | "temporal" | "semantic";
  participants: string[];                    // Agent IDs
  severity: "low" | "medium" | "high" | "critical";
  resolution: ResolutionStrategy;
}
```

#### Resolution Strategies
```typescript
interface ResolutionStrategy {
  immediate: boolean;                       // Requires immediate action
  strategy: "preempt" | "delay" | "share" | "alternate" | "user_decide";
  parameters: Record<string, any>;
  timeout?: number;                         // Resolution timeout
  fallback?: ResolutionStrategy;            // Backup strategy
}
```

### 3.5 Decision Audit Trail

#### Audit Event Structure
```typescript
interface AuditEvent {
  id: string;
  timestamp: Date;
  type: "decision" | "override" | "conflict" | "resolution";
  participants: string[];                    // Involved agents
  decision: {
    reasoning: string;                      // Decision rationale
    factors: DecisionFactor[];              // Decision factors
    confidence: number;                     // Confidence score (0-1)
    alternatives: Alternative[];            // Considered alternatives
  };
  outcome: {
    result: "success" | "failure" | "partial" | "timeout";
    impact: ImpactAssessment;
    lessons: string[];                      // Learnings for future decisions
  };
}
```

## 4. Platform Abstraction Layer (PAL) Interface Definition

### 4.1 System Access Contracts

#### Platform Capabilities
```typescript
interface PlatformCapabilities {
  operatingSystem: OS;
  version: string;
  permissions: PermissionModel;
  accessibility: AccessibilityAPI;
  backgroundExecution: BackgroundAPI;
  networking: NetworkAPI;
  storage: StorageAPI;
  audio: AudioAPI;
  ui: UIAPI;
  security: SecurityAPI;
}

enum OS {
  ANDROID = "android",
  IOS = "ios",
  MACOS = "macos",
  WINDOWS = "windows",
  LINUX = "linux"
}
```

#### Generic System Interface
```typescript
interface SystemAPI {
  // Process management
  createProcess(config: ProcessConfig): Promise<ProcessHandle>;
  terminateProcess(handle: ProcessHandle): Promise<void>;
  getProcessInfo(handle: ProcessHandle): Promise<ProcessInfo>;
  
  // File system access
  readFile(path: string): Promise<Buffer>;
  writeFile(path: string, data: Buffer): Promise<void>;
  deleteFile(path: string): Promise<void>;
  listDirectory(path: string): Promise<FileInfo[]>;
  
  // System information
  getSystemInfo(): Promise<SystemInfo>;
  getResourceUsage(): Promise<ResourceUsage>;
  
  // Configuration
  getConfig(key: string): Promise<any>;
  setConfig(key: string, value: any): Promise<void>;
}
```

### 4.2 Bluetooth/Earbud Communication Interface

#### Bluetooth Protocol
```typescript
interface BluetoothAPI {
  // Device discovery and connection
  scanDevices(filters?: DeviceFilter[]): Promise<BluetoothDevice[]>;
  connectDevice(deviceId: string): Promise<BluetoothConnection>;
  disconnectDevice(deviceId: string): Promise<void>;
  
  // Earbud-specific features
  getEarbudStatus(): Promise<EarbudStatus>;
  sendAudioCommand(command: AudioCommand): Promise<void>;
  receiveAudioData(): AsyncIterator<AudioData>;
  
  // Low-latency communication
  createLowLatencyChannel(deviceId: string): Promise<LowLatencyChannel>;
  sendPriorityData(channel: LowLatencyChannel, data: Buffer): Promise<void>;
}

interface EarbudStatus {
  left: {
    battery: number;
    connected: boolean;
    latency: number;                      // ms
  };
  right: {
    battery: number;
    connected: boolean;
    latency: number;
  };
  paired: boolean;
  audioProfile: AudioProfile;
}
```

### 4.3 Accessibility & Notification Interception API

#### Accessibility Service Interface
```typescript
interface AccessibilityAPI {
  // Screen reader and accessibility
  enableAccessibilityService(config: AccessibilityConfig): Promise<void>;
  disableAccessibilityService(): Promise<void>;
  
  // UI element access
  queryUIelements(filter: UIElementFilter): Promise<UIElement[]>;
  performAction(element: UIElement, action: UIAction): Promise<boolean>;
  captureScreen(): Promise<ScreenCapture>;
  
  // Input simulation
  simulateTouch(x: number, y: number): Promise<void>;
  simulateKey(key: string, modifiers?: KeyModifier[]): Promise<void>;
  simulateGesture(gesture: Gesture): Promise<void>;
  
  // Event monitoring
  monitorUIEvents(filter?: UIEventFilter): AsyncIterator<UIEvent>;
}

interface AccessibilityConfig {
  serviceName: string;
  packageName: string;
  eventTypes: AccessibilityEventType[];
  feedbackTypes: FeedbackType[];
  flags: AccessibilityServiceFlag[];
}
```

#### Notification Interception
```typescript
interface NotificationAPI {
  // Notification monitoring
  enableNotificationListener(config: NotificationConfig): Promise<void>;
  disableNotificationListener(): Promise<void>;
  
  // Notification access
  getActiveNotifications(): Promise<Notification[]>;
  getNotificationHistory(): Promise<Notification[]>;
  
  // Notification control
  postNotification(notification: Notification): Promise<void>;
  cancelNotification(notificationId: string): Promise<void>;
  
  // Event stream
  monitorNotifications(filter?: NotificationFilter): AsyncIterator<NotificationEvent>;
}

interface NotificationConfig {
  packageName: string;
  notificationCategories: NotificationCategory[];
  interceptLevel: "none" | "metadata" | "content" | "full";
}
```

### 4.4 Audio I/O Abstraction

#### Audio Interface
```typescript
interface AudioAPI {
  // Audio capture
  startRecording(config: RecordingConfig): Promise<AudioStream>;
  stopRecording(streamId: string): Promise<AudioRecording>;
  
  // Audio playback
  playAudio(audio: AudioData, config?: PlaybackConfig): Promise<PlaybackHandle>;
  stopPlayback(handle: PlaybackHandle): Promise<void>;
  
  // Voice processing
  recognizeSpeech(audio: AudioData, config: SpeechConfig): Promise<SpeechResult>;
  synthesizeVoice(text: string, config: VoiceConfig): Promise<AudioData>;
  detectVoiceActivity(audio: AudioData): Promise<VoiceActivityResult>;
  
  // Audio routing
  setAudioRoute(route: AudioRoute): Promise<void>;
  getAudioRoute(): Promise<AudioRoute>;
  monitorAudioRouteChanges(): AsyncIterator<AudioRouteChange>;
}

interface RecordingConfig {
  sampleRate: number;
  channels: number;
  bitDepth: number;
  bufferSize: number;
  filters: AudioFilter[];
  noiseSuppression: boolean;
  echoCancellation: boolean;
}
```

### 4.5 ADB/Admin Access Abstraction

#### Debug and Admin Interface
```typescript
interface AdminAPI {
  // ADB connection (when enabled)
  connectADB(config: ADBConfig): Promise<ADBConnection>;
  executeADBCommand(command: string, timeout?: number): Promise<ADBResult>;
  
  // Administrative functions (when authorized)
  getSystemProperties(): Promise<Record<string, string>>;
  setSystemProperties(props: Record<string, string>): Promise<void>;
  killProcess(processName: string): Promise<boolean>;
  
  // Development tools
  enableDeveloperMode(): Promise<boolean>;
  installApp(packagePath: string, flags?: InstallFlags): Promise<boolean>;
  uninstallApp(packageName: string): Promise<boolean>;
  
  // System monitoring
  getSystemLogs(filter?: LogFilter): AsyncIterator<LogEntry>;
  monitorPerformance(): AsyncIterator<PerformanceMetric>;
}

interface ADBConfig {
  host: string;
  port: number;
  timeout: number;
  retries: number;
  authentication?: ADBAuth;
}
```

### 4.6 Platform-Specific Implementations

#### Android Implementation
```typescript
class AndroidPAL implements PlatformAbstractionLayer {
  private accessibility: AccessibilityService;
  private notification: NotificationListenerService;
  private audio: AudioManager;
  private bluetooth: BluetoothManager;
  private admin: DevicePolicyManager;
  
  // Implementation details for Android-specific APIs
  async enableAccessibilityService(config: AccessibilityConfig): Promise<void> {
    // Use AccessibilityService API
  }
  
  async connectEarbuds(): Promise<EarbudConnection> {
    // Use Bluetooth API with low-latency audio profile
  }
}
```

#### iOS Implementation
```typescript
class iOSPAL implements PlatformAbstractionLayer {
  private accessibility: AccessibilityAPI;
  private audio: AVAudioSession;
  private bluetooth: CoreBluetooth;
  private shortcuts: ShortcutsApp;
  
  // iOS-specific limitations and capabilities
  // Note: Some Android features may not be available
}
```

## 5. Security Primitives & Model

### 5.1 Earbuds as Physical Authentication Token

#### Authentication Protocol
```typescript
interface EarbudAuth {
  // Device pairing and verification
  pairEarbuds(deviceId: string): Promise<PairingResult>;
  verifyEarbudPresence(): Promise<PresenceResult>;
  challengeResponse(challenge: Buffer): Promise<Buffer>;
  
  // Biometric signals (presence-based)
  detectWearing(): Promise<boolean>;
  getMovementProfile(): Promise<MovementProfile>;
  detectVoicePrint(): Promise<VoicePrintResult>;
  
  // Security properties
  getSecurityLevel(): Promise<SecurityLevel>;
  revokeTrust(): Promise<void>;
  rotateKeys(): Promise<void>;
}

interface PresenceProof {
  deviceId: string;
  timestamp: Date;
  challenge: Buffer;
  response: Buffer;
  signalStrength: number;
  batteryLevel: number;
}
```

### 5.2 Voiceprint Verification Workflow

#### Voice Authentication
```typescript
interface VoiceAuth {
  // Enrollment
  enrollVoiceprint(userId: string, samples: AudioSample[]): Promise<EnrollmentResult>;
  updateVoiceprint(userId: string, samples: AudioSample[]): Promise<UpdateResult>;
  
  // Verification
  verifyVoice(audio: AudioData, userId?: string): Promise<VerificationResult>;
  getVoiceFeatures(audio: AudioData): Promise<VoiceFeatures>;
  
  // Management
  deleteVoiceprint(userId: string): Promise<void>;
  listVoiceprints(): Promise<UserId[]>;
}

interface VoiceFeatures {
  spectral: number[];
  mfcc: number[];
  pitch: number[];
  formant: number[];
  quality: number;                       // Voice quality score
}
```

### 5.3 Encryption Contracts

#### Encryption Architecture
```typescript
interface EncryptionConfig {
  // Key management
  keyRotation: {
    enabled: boolean;
    interval: number;                     // hours
    algorithm: KeyRotationAlgorithm;
  };
  
  // Encryption algorithms
  symmetric: {
    algorithm: "AES-256-GCM" | "ChaCha20-Poly1305";
    keyLength: number;
  };
  
  asymmetric: {
    algorithm: "RSA-4096" | "ECC-P256";
    keyLength: number;
  };
  
  // Storage encryption
  atRest: {
    database: EncryptionConfig;
    files: EncryptionConfig;
    memory: EncryptionConfig;
  };
  
  // Transport encryption
  inTransit: {
    protocol: "TLS-1.3" | "Noise";
    certificatePinning: boolean;
    perfectForwardSecrecy: boolean;
  };
}
```

### 5.4 Permission Boundaries Per Agent

#### Permission Model
```typescript
interface AgentPermissions {
  agentId: string;
  permissions: Permission[];
  restrictions: Restriction[];
  temporalConstraints: TemporalConstraint[];
  resourceLimits: ResourceLimit[];
}

interface Permission {
  name: string;
  level: "read" | "write" | "execute" | "admin";
  scope: string[];
  conditions: PermissionCondition[];
}

interface Restriction {
  type: "time" | "location" | "user_present" | "battery_level";
  parameters: Record<string, any>;
  enforcement: "soft" | "hard";
}
```

### 5.5 Offline-First Security Assumptions

#### Offline Security Model
```typescript
interface OfflineSecurity {
  // Local key storage
  storeKeysSecurely(keys: CryptoKeys): Promise<void>;
  retrieveKeys(): Promise<CryptoKeys>;
  secureDeleteKeys(): Promise<void>;
  
  // Offline authentication
  offlineChallenge(challenge: Buffer): Promise<Buffer>;
  validateOfflineProof(proof: PresenceProof): Promise<boolean>;
  
  // Secure communication
  establishSecureChannel(peerId: string): Promise<SecureChannel>;
  encryptMessage(message: any, channel: SecureChannel): Promise<EncryptedMessage>;
}

interface SecureChannel {
  peerId: string;
  sessionKey: Buffer;
  expiresAt: Date;
  usageCount: number;
  maxUsage: number;
}
```

### 5.6 Telemetry Opt-Out Guarantees

#### Privacy Controls
```typescript
interface PrivacyConfig {
  // Telemetry settings
  telemetry: {
    enabled: boolean;
    categories: TelemetryCategory[];
    retention: RetentionPolicy;
  };
  
  // Data collection controls
  dataCollection: {
    audioRecording: boolean;
    voiceprintStorage: boolean;
    behavioralPatterns: boolean;
    systemInteraction: boolean;
  };
  
  // User controls
  userControls: {
    granularOptOut: boolean;
    realTimeRevocation: boolean;
    dataPortability: boolean;
    auditTrail: boolean;
  };
}

interface TelemetryCategory {
  name: string;
  enabled: boolean;
  retention: number;                      // days
  anonymized: boolean;
}
```

## 6. Execution Contracts

### 6.1 Capability Matrix

#### Agent Capabilities
```typescript
interface CapabilityMatrix {
  // System access
  systemCalls: {
    fileSystem: AccessLevel;
    network: AccessLevel;
    process: AccessLevel;
    registry: AccessLevel;
  };
  
  // Platform integration
  accessibility: boolean;
  notifications: boolean;
  audioRecording: boolean;
  audioPlayback: boolean;
  bluetooth: boolean;
  usbDebugging: boolean;
  
  // Data access
  contacts: AccessLevel;
  calendar: AccessLevel;
  location: AccessLevel;
  microphone: AccessLevel;
  camera: AccessLevel;
  
  // Execution constraints
  backgroundExecution: boolean;
  realTimeExecution: boolean;
  systemLevelExecution: boolean;
}

enum AccessLevel {
  NONE = "none",
  READ = "read",
  WRITE = "write",
  ADMIN = "admin"
}
```

### 6.2 Forbidden Operations

#### Security Boundaries
```typescript
interface SecurityBoundaries {
  // Absolute prohibitions
  forbiddenOperations: ForbiddenOp[];
  
  // Conditional restrictions
  restrictedOperations: RestrictedOp[];
  
  // Sandboxing requirements
  sandboxRequirements: SandboxRequirement[];
}

interface ForbiddenOp {
  operation: string;
  reason: "security" | "privacy" | "legal" | "platform_policy";
  detection: "static" | "dynamic" | "both";
  penalty: "agent_termination" | "permission_revoke" | "system_alert";
}

interface RestrictedOp {
  operation: string;
  conditions: Condition[];
  maxFrequency: number;                   // operations per hour
  requiresConfirmation: boolean;
  escalationRequired: boolean;
}
```

### 6.3 Sandboxing & Isolation Rules

#### Isolation Architecture
```typescript
interface IsolationConfig {
  // Process isolation
  processIsolation: {
    enabled: boolean;
    sandboxType: "none" | "seccomp" | "apparmor" | "sandboxie";
    resourceLimits: ResourceLimits;
  };
  
  // Memory isolation
  memoryIsolation: {
    stackProtection: boolean;
    heapProtection: boolean;
    aslrEnabled: boolean;
    stackCanaries: boolean;
  };
  
  // Network isolation
  networkIsolation: {
    allowedPorts: number[];
    allowedHosts: string[];
    protocolRestrictions: ProtocolRestriction[];
    dnsFiltering: boolean;
  };
  
  // File system isolation
  fsIsolation: {
    allowedPaths: string[];
    readOnlyPaths: string[];
    temporaryPaths: string[];
    mountNamespace: boolean;
  };
}
```

### 6.4 Background Execution Constraints

#### Background Limits
```typescript
interface BackgroundConstraints {
  // Android constraints
  android: {
    dozeMode: "full" | "partial" | "none";
    appStandby: "active" | "working_set" | "frequent" | "rare";
    backgroundLimits: "strict" | "moderate" | "relaxed";
  };
  
  // iOS constraints
  ios: {
    backgroundAppRefresh: boolean;
    backgroundProcessing: boolean;
    backgroundFetch: boolean;
    remoteNotifications: boolean;
  };
  
  // General constraints
  general: {
    maxExecutionTime: number;            // minutes per hour
    cpuThrottling: number;                // percentage (0-100)
    memoryThrottling: number;             // percentage reduction
    networkBudget: number;                // bytes per day
  };
}
```

## 7. Build System Specification

### 7.1 Feature Flags

#### Configuration System
```typescript
interface FeatureFlags {
  // Platform flags
  platforms: {
    android: boolean;
    ios: boolean;
    macos: boolean;
    windows: boolean;
    linux: boolean;
  };
  
  // Agent type flags
  agents: {
    security: boolean;
    executor: boolean;
    listener: boolean;
    context: boolean;
    ui: boolean;
    analytics: boolean;
  };
  
  // Capability flags
  capabilities: {
    voiceRecognition: boolean;
    speechSynthesis: boolean;
    accessibility: boolean;
    notificationInterception: boolean;
    bluetoothIntegration: boolean;
    adbAccess: boolean;
  };
  
  // Debug/development flags
  debug: {
    verboseLogging: boolean;
    performanceProfiling: boolean;
    securityAuditing: boolean;
    mockServices: boolean;
  };
}
```

### 7.2 Model Injection Pipeline

#### ML Model Management
```typescript
interface ModelPipeline {
  // Model sources
  sources: {
    local: string[];                      // Local model paths
    remote: RemoteModelSource[];
    custom: CustomModelSource[];
  };
  
  // Model validation
  validation: {
    checksumValidation: boolean;
    signatureValidation: boolean;
    runtimeValidation: boolean;
  };
  
  // Model optimization
  optimization: {
    quantization: boolean;
    pruning: boolean;
    onDeviceOptimization: boolean;
  };
  
  // Model updates
  updates: {
    autoUpdate: boolean;
    updateCheckInterval: number;          // hours
    rollbackEnabled: boolean;
  };
}

interface RemoteModelSource {
  name: string;
  url: string;
  auth: AuthMethod;
  format: "onnx" | "tflite" | "pytorch" | "tensorflow";
  version: string;
}
```

### 7.3 Cross-Platform Compilation Strategy

#### Build Configuration
```typescript
interface BuildConfig {
  // Target platforms
  targets: PlatformTarget[];
  
  // Compiler settings
  compilers: {
    android: AndroidCompilerConfig;
    ios: IOSCompilerConfig;
    windows: WindowsCompilerConfig;
    macos: MacOSCompilerConfig;
    linux: LinuxCompilerConfig;
  };
  
  // Shared libraries
  sharedLibs: {
    enabled: boolean;
    minVersion: Record<string, string>;
    excludedLibs: string[];
  };
  
  // Optimization levels
  optimization: {
    release: OptimizationLevel;
    debug: OptimizationLevel;
    profile: OptimizationLevel;
  };
}

interface PlatformTarget {
  platform: OS;
  architecture: string;
  minimumVersion: string;
  optimizations: string[];
  excludedFeatures: string[];
}
```

### 7.4 License Binding & Version Management

#### License Management
```typescript
interface LicenseConfig {
  // Component licenses
  components: {
    name: string;
    license: LicenseType;
    version: string;
    attribution: AttributionConfig;
    copyleft: boolean;
  }[];
  
  // Distribution requirements
  distribution: {
    commercial: boolean;
    openSource: boolean;
    academic: boolean;
    personal: boolean;
  };
  
  // Attribution requirements
  attribution: {
    enabled: boolean;
    format: "text" | "html" | "json";
    includeSource: boolean;
  };
}

enum LicenseType {
  MIT = "mit",
  APACHE_2 = "apache-2",
  GPL_3 = "gpl-3",
  BSD_3 = "bsd-3",
  COMMERCIAL = "commercial",
  PROPRIETARY = "proprietary"
}
```

### 7.5 Root vs Non-Root Build Variants

#### Build Variants
```typescript
interface BuildVariant {
  name: string;
  permissions: PermissionSet;
  capabilities: CapabilitySet;
  platformRequirements: PlatformRequirement[];
  securityLevel: SecurityLevel;
  distribution: DistributionConfig;
}

interface PermissionSet {
  standard: string[];                     // Standard user permissions
  elevated: string[];                     // Elevated permissions (non-root)
  root: string[];                        // Root-only permissions
  system: string[];                      // System-level permissions
}

interface SecurityLevel {
  name: "low" | "medium" | "high" | "maximum";
  sandboxing: boolean;
  encryption: boolean;
  auditLogging: boolean;
  codeSigning: boolean;
  attestation: boolean;
}
```

## 8. Repository Structure & Monorepo Layout

### 8.1 Top-Level Organization

```
mecka/
├── README.md
├── LICENSE
├── CONTRIBUTING.md
├── .gitignore
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── PULL_REQUEST_TEMPLATE/
│   └── workflows/
│       ├── ci.yml
│       ├── security.yml
│       └── release.yml
├── docs/
│   ├── architecture/
│   ├── api/
│   ├── deployment/
│   ├── security/
│   └── tutorials/
├── examples/
│   ├── android/
│   ├── ios/
│   ├── desktop/
│   └── plugins/
├── packages/
│   ├── core/                    # Shared core libraries
│   ├── platform/                # Platform abstractions
│   ├── agents/                  # Agent implementations
│   ├── security/                 # Security components
│   ├── audio/                   # Audio processing
│   ├── ui/                      # User interface
│   └── utils/                   # Utility libraries
└── tools/
    ├── build/
    ├── deploy/
    ├── test/
    └── analysis/
```

### 8.2 Android Implementation Folder Structure

```
android/
├── README.md
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/mecka/
│   │   │   │   ├── accessibility/
│   │   │   │   ├── audio/
│   │   │   │   ├── bluetooth/
│   │   │   │   ├── notification/
│   │   │   │   ├── security/
│   │   │   │   ├── ui/
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   ├── values/
│   │   │   │   └── xml/
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/
│   │   └── test/
│   ├── build.gradle
│   └── proguard-rules.pro
├── accessibility/
│   ├── src/
│   ├── build.gradle
│   └── README.md
├── bluetooth/
│   ├── src/
│   ├── build.gradle
│   └── README.md
├── audio/
│   ├── src/
│   ├── build.gradle
│   └── README.md
└── notification/
    ├── src/
    ├── build.gradle
    └── README.md
```

### 8.3 Shared Core Libraries

```
core/
├── agent-runtime/
│   ├── src/
│   │   ├── lifecycle/
│   │   ├── scheduling/
│   │   ├── error-handling/
│   │   └── resource-management/
│   ├── test/
│   ├── build.gradle
│   └── README.md
├── ipc/
│   ├── src/
│   │   ├── message/
│   │   ├── routing/
│   │   ├── serialization/
│   │   └── state-bus/
│   ├── test/
│   ├── build.gradle
│   └── README.md
├── security/
│   ├── src/
│   │   ├── authentication/
│   │   ├── authorization/
│   │   ├── encryption/
│   │   └── audit/
│   ├── test/
│   ├── build.gradle
│   └── README.md
└── common/
    ├── src/
    │   ├── types/
    │   ├── utils/
    │   ├── config/
    │   └── logging/
    ├── test/
    ├── build.gradle
    └── README.md
```

### 8.4 Platform-Specific Abstraction Implementations

```
platform/
├── android/
│   ├── src/
│   │   ├── accessibility/
│   │   ├── audio/
│   │   ├── bluetooth/
│   │   ├── notification/
│   │   ├── system/
│   │   └── security/
│   ├── test/
│   ├── build.gradle
│   └── README.md
├── ios/
│   ├── src/
│   │   ├── accessibility/
│   │   ├── audio/
│   │   ├── bluetooth/
│   │   ├── system/
│   │   └── security/
│   ├── test/
│   ├── build.gradle
│   └── README.md
├── windows/
│   ├── src/
│   ├── test/
│   ├── build.gradle
│   └── README.md
├── macos/
│   ├── src/
│   ├── test/
│   ├── build.gradle
│   └── README.md
└── linux/
    ├── src/
    ├── test/
    ├── build.gradle
    └── README.md
```

### 8.5 Documentation, Tests, Examples Organization

```
docs/
├── architecture/
│   ├── overview.md
│   ├── agent-lifecycle.md
│   ├── ipc-protocol.md
│   ├── security-model.md
│   └── platform-abstraction.md
├── api/
│   ├── reference/
│   ├── examples/
│   └── guides/
├── deployment/
│   ├── installation.md
│   ├── configuration.md
│   ├── security-setup.md
│   └── troubleshooting.md
└── tutorials/
    ├── getting-started/
    ├── building-agents/
    ├── platform-integration/
    └── security-best-practices/

test/
├── unit/
│   ├── core/
│   ├── platform/
│   ├── agents/
│   └── security/
├── integration/
│   ├── android/
│   ├── ios/
│   └── cross-platform/
├── performance/
├── security/
└── fuzz/

examples/
├── simple-agent/
├── audio-processing/
├── accessibility-service/
├── bluetooth-integration/
└── cross-platform-demo/
```

### 8.6 CI/CD Pipeline Scaffolding

```
.github/workflows/
├── ci.yml                    # Continuous Integration
│   ├── unit-tests
│   ├── integration-tests
│   ├── security-scans
│   ├── performance-tests
│   └── code-quality
├── security.yml              # Security Pipeline
│   ├── vulnerability-scan
│   ├── dependency-check
│   ├── license-compliance
│   └── security-audit
├── release.yml               # Release Pipeline
│   ├── build-artifacts
│   ├── sign-releases
│   ├── publish-packages
│   └── generate-docs
└── deployment.yml            # Deployment Pipeline
    ├── staging-deployment
    ├── production-deployment
    ├── rollback-procedures
    └── monitoring-setup
```

## 9. Android as Reference Implementation Strategy

### 9.1 Why Android as Stress-Test

#### Android as Comprehensive Test Platform
Android provides the most comprehensive API surface for testing agentic operating intelligence due to:

1. **Rich Permission Model**: Granular runtime permissions allow testing of permission boundaries
2. **Accessibility Services**: Full accessibility API enables comprehensive UI interaction testing
3. **Background Execution Limits**: Strict background execution limits test resource management
4. **Notification Interception**: Complete notification API allows testing of event-driven architectures
5. **Audio Processing**: Comprehensive audio APIs test voice-centric features
6. **Bluetooth Integration**: Full Bluetooth stack testing, including BLE and audio profiles
7. **Security Features**: SELinux, app sandboxing, and permission enforcement
8. **Developer Tools**: ADB, debugging APIs, and development mode access

#### Key Android-Specific Challenges
- **Permission Runtime Model**: Testing permission acquisition, denial, and revocation
- **Doze Mode & App Standby**: Testing background execution limitations
- **Accessibility Service Integration**: Complex accessibility event handling
- **Notification Listener Service**: Testing notification interception and processing
- **Audio Focus Management**: Handling audio routing and focus conflicts
- **Service Lifecycle**: Managing bound and started services

### 9.2 PAL Design for Cross-Platform Translation

#### Abstraction Strategy
```typescript
interface PlatformAbstractionLayer {
  // Core interfaces that must be implemented per platform
  systemAPI: SystemAPI;
  audioAPI: AudioAPI;
  bluetoothAPI: BluetoothAPI;
  accessibilityAPI: AccessibilityAPI;
  notificationAPI: NotificationAPI;
  securityAPI: SecurityAPI;
  uiAPI: UIAPI;
}

// Platform-specific implementations
class AndroidPAL implements PlatformAbstractionLayer { /* Android-specific */ }
class iOSPAL implements PlatformAbstractionLayer { /* iOS-specific */ }
class WindowsPAL implements PlatformAbstractionLayer { /* Windows-specific */ }
```

#### Feature Availability Matrix
```
Feature                    | Android | iOS | macOS | Windows | Linux
Accessibility Service      |   ✓    |  ⚠  |   ✓   |    ✓    |   ✓
Notification Listener      |   ✓    |  ✗  |   ⚠   |    ⚠    |   ⚠
Audio Recording            |   ✓    |  ✓  |   ✓   |    ✓    |   ✓
Speech Recognition         |   ✓    |  ✓  |   ✓   |    ✓    |   ✓
ADB Debugging              |   ✓    |  ✗  |   ⚠   |    ✓    |   ✓
Background Execution       |   ⚠    |  ⚠  |   ✓   |    ✓    |   ✓
Bluetooth Integration      |   ✓    |  ✓  |   ✓   |    ✓    |   ✓

Legend: ✓ = Full support, ⚠ = Partial support, ✗ = Not available
```

### 9.3 Key Android Technical Decisions

#### Accessibility Service Implementation
```kotlin
class MeckaAccessibilityService : AccessibilityService() {
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Process accessibility events
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> handleClick(event)
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> handleScroll(event)
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> handleNotification(event)
            else -> handleOtherEvent(event)
        }
    }
    
    override fun onInterrupt() {
        // Handle service interruption
        notificationManager.notify(SERVICE_INTERRUPTED, notification)
    }
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        // Initialize service configuration
        configureAccessibilityFeedback()
        registerUIMonitoring()
    }
}
```

#### Notification Listener Service
```kotlin
class MeckaNotificationListener : NotificationListenerService() {
    
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // Process new notifications
        val notification = convertToMeckaNotification(sbn)
        
        // Filter based on user preferences
        if (notificationFilter.shouldProcess(notification)) {
            stateBus.publish("notifications", notification, Priority.NORMAL)
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // Handle notification removal
        val notification = convertToMeckaNotification(sbn)
        stateBus.publish("notifications.removed", notification, Priority.LOW)
    }
}
```

#### ADB Bridge Implementation
```kotlin
class MeckaADBBridge {
    
    fun executeADBCommand(command: String): ADBResult {
        return try {
            val process = ProcessBuilder("adb", command.split(" "))
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            ADBResult(exitCode == 0, output, null)
        } catch (e: Exception) {
            ADBResult(false, "", e.message)
        }
    }
    
    fun installApp(packagePath: String, flags: String = "-r"): Boolean {
        return executeADBCommand("install $flags $packagePath").success
    }
    
    fun getSystemProperties(): Map<String, String> {
        val result = executeADBCommand("shell getprop")
        return parseProperties(result.output)
    }
}
```

### 9.4 Validation Milestones for Android Baseline

#### Phase 1: Core Infrastructure
- [ ] Basic agent runtime and lifecycle management
- [ ] IPC message bus implementation
- [ ] Permission system integration
- [ ] Basic audio capture and playback

#### Phase 2: Platform Integration
- [ ] Accessibility Service implementation
- [ ] Notification Listener Service implementation
- [ ] Bluetooth integration with earbuds
- [ ] Voice recognition and synthesis

#### Phase 3: Security & Arbitration
- [ ] Security primitive implementation
- [ ] Agent arbitration system
- [ ] Conflict resolution mechanisms
- [ ] Audit trail system

#### Phase 4: Advanced Features
- [ ] ADB bridge implementation
- [ ] Background execution optimization
- [ ] Cross-platform abstraction layer
- [ ] Performance optimization and testing

#### Phase 5: Production Readiness
- [ ] Security audit and penetration testing
- [ ] Performance benchmarking
- [ ] User experience testing
- [ ] Documentation and deployment preparation

## 10. Documentation & Diagrams

### 10.1 Agent Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        Mecka Agent System                       │
├─────────────────────────────────────────────────────────────────┤
│  User Interface Layer                                            │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐                │
│  │   Voice UI  │ │  Touch UI   │ │  Status UI  │                │
│  └─────────────┘ └─────────────┘ └─────────────┘                │
├─────────────────────────────────────────────────────────────────┤
│  Agent Orchestration Layer                                       │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              Arbitration & Conflict Resolution                ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             ││
│  │  │  Priority   │ │  Deadlock  │ │  Decision   │             ││
│  │  │  Management │ │ Prevention │ │   Audit     │             ││
│  │  └─────────────┘ └─────────────┘ └─────────────┘             ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Agent Execution Layer                                           │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│  │  Security   │ │  Executor   │ │  Listener   │ │  Context    ││
│  │   Agent     │ │   Agent     │ │   Agent     │ │   Agent     ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐│
│  │     UI      │ │  Analytics  │ │   Custom    │ │   Custom    ││
│  │   Agent     │ │   Agent     │ │   Agent     │ │   Agent     ││
│  └─────────────┘ └─────────────┘ └─────────────┘ └─────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Communication Layer                                              │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │              IPC Message Bus & State Bus                     ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             ││
│  │  │   Message   │ │    State    │ │  Priority   │             ││
│  │  │  Routing    │ │ Management  │ │   Queue     │             ││
│  │  └─────────────┘ └─────────────┘ └─────────────┘             ││
│  └─────────────────────────────────────────────────────────────┘│
├─────────────────────────────────────────────────────────────────┤
│  Platform Abstraction Layer (PAL)                               │
│  ┌─────────────────────────────────────────────────────────────┐│
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             ││
│  │  │ Accessibility│ │  Bluetooth  │ │   Audio     │             ││
│  │  │    API      │ │     API     │ │     API     │             ││
│  │  └─────────────┘ └─────────────┘ └─────────────┘             ││
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐             ││
│  │  │Notification │ │    ADB      │ │  Security   │             ││
│  │  │     API     │ │     API     │ │     API     │             ││
│  │  └─────────────┘ └─────────────┘ └─────────────┘             ││
│  └─────────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

### 10.2 IPC & State Bus Flow Diagram

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ Agent A      │    │    IPC      │    │ Agent B     │
│  (Executor)  │────│   Router    │────│  (Listener) │
└─────────────┘    └─────────────┘    └─────────────┘
                           │
                           ▼
                   ┌─────────────┐
                   │   State     │
                   │    Bus      │
                   │  ┌───────┐  │
                   │  │ Pub/Sub│  │
                   │  │Topics │  │
                   │  └───────┘  │
                   │  ┌───────┐  │
                   │  │Priority│  │
                   │  │ Queue │  │
                   │  └───────┘  │
                   └─────────────┘
                           │
                           ▼
                   ┌─────────────┐
                   │  Serialization│
                   │ & Compression │
                   └─────────────┘

Message Flow:
1. Agent A creates message
2. IPC Router validates and routes
3. State Bus manages delivery
4. Serialization/Compression
5. Agent B receives and processes
```

### 10.3 Arbitration Decision Tree

```
┌─────────────────────────────────────┐
│        New Intent Received           │
└─────────────┬───────────────────────┘
              │
              ▼
    ┌─────────────────────────┐
    │   Check Resource        │
    │     Availability        │
    └─────────┬───────────────┘
              │
     Available? ◄────┐
     ┌───────┐       │
     │  No   │       │ Yes
     ▼       │       ▼
┌─────────┐ │   ┌─────────────────┐
│ Check   │ │   │   Check Agent   │
│Priority │ │   │     Status      │
└────┬────┘ │   └─────────┬───────┘
     │      │             │
     ▼      │             ▼
┌─────────┐ │        ┌─────────────┐
│ Higher  │ │        │   Ready &   │
│Priority │ │        │ Authorized? │
│Agent?   │ │        └──────┬──────┘
└────┬────┘ │               │
     │      │              No
     │      │               │
     ▼      │               ▼
┌─────────┐ │        ┌─────────────┐
│Preempt  │ │        │   Queue     │
│Lower    │ │        │   Intent    │
│Priority │ │        └─────────────┘
└────┬────┘ │
     │      │
     ▼      │
┌─────────┐ │
│ Execute │ │
│ Intent  │ │
└────┬────┘ │
     │      │
     ▼      │
┌─────────┐ │
│Monitor  │ │
│Progress │ │
└────────┘ │
```

### 10.4 PAL Abstraction Layers Diagram

```
┌─────────────────────────────────────────────────────────┐
│              Application Layer                          │
│           (Agent Implementations)                        │
└─────────────────┬─────────────────────────────────────────┘
                  │
┌─────────────────▼─────────────────────────────────────────┐
│            Platform Abstraction Interface                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │   System    │ │    Audio    │ │  Bluetooth  │          │
│  │     API     │ │     API     │ │     API     │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐          │
│  │Accessibility│ │Notification │ │   Security  │          │
│  │     API     │ │     API     │ │     API     │          │
│  └─────────────┘ └─────────────┘ └─────────────┘          │
└─────────────────┬─────────────────────────────────────────┘
                  │
        ┌─────────┴─────────┐
        │                   │
┌───────▼─────────┐  ┌──────▼────────┐
│   Android       │  │      iOS       │
│   Platform      │  │    Platform    │
│   Implementation│  │ Implementation │
│                 │  │                │
│ ┌─────────────┐ │  │ ┌────────────┐ │
│ │Accessibility│ │  │ │Accessibility│ │
│ │   Service   │ │  │ │   Service  │ │
│ └─────────────┘ │  │ └────────────┘ │
│ ┌─────────────┐ │  │ ┌────────────┐ │
│ │Notification │ │  │ │  AVAudio   │ │
│ │   Listener  │ │  │ │   Session  │ │
│ └─────────────┘ │  │ └────────────┘ │
│ ┌─────────────┐ │  │ ┌────────────┐ │
│ │  Bluetooth  │ │  │ │ CoreBluetooth│ │
│ │   Manager   │ │  │ │             │ │
│ └─────────────┘ │  │ └────────────┘ │
└─────────────────┘  └────────────────┘
```

### 10.5 Security Model Threat Model

```
┌─────────────────────────────────────────────────────────────────┐
│                      Threat Model Analysis                      │
├─────────────────────────────────────────────────────────────────┤
│  Threat Categories                                              │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  Eavesdropping   │  │   Impersonation│  │   Data Tampering │ │
│  │                 │  │                 │  │                 │ │
│  │ • Network traffic│  │ • Fake agents  │  │ • Message       │ │
│  │ • Audio streams  │  │ • Spoofed ID   │  │   modification  │ │
│  │ • State bus      │  │ • Permission   │  │ • Config        │ │
│  │   messages      │  │   escalation   │  │   corruption    │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Denial of     │  │  Privilege      │  │   Information   │ │
│  │    Service      │  │  Escalation     │  │    Disclosure   │ │
│  │                 │  │                 │  │                 │ │
│  │ • Resource      │  │ • Unauthorized  │  │ • Voiceprint    │ │
│  │   exhaustion    │  │   access        │  │   data          │ │
│  │ • Agent         │  │ • Permission    │  │ • User patterns │ │
│  │   termination   │  │   bypass        │  │ • System info   │ │
│  │ • System        │  │ • Sandbox       │  │ • Location data │ │
│  │   instability   │  │   escape        │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
├─────────────────────────────────────────────────────────────────┤
│  Security Controls                                             │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │  Encryption      │  │ Authentication  │  │   Authorization │ │
│  │                 │  │                 │  │                 │ │
│  │ • TLS 1.3       │  │ • Earbud auth   │  │ • RBAC          │ │
│  │ • AES-256-GCM    │  │ • Voiceprint    │  │ • Permissions   │ │
│  │ • End-to-end    │  │ • Certificate   │  │ • Sandboxing    │ │
│  │ • Key rotation  │  │   pinning       │  │ • Isolation     │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                                                                 │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │   Monitoring    │  │   Audit &       │  │   Recovery &    │ │
│  │   & Detection   │  │   Compliance     │  │   Resilience    │ │
│  │                 │  │                 │  │                 │ │
│  │ • Anomaly       │  │ • Audit trail   │  │ • Graceful      │ │
│  │   detection     │  │ • Compliance    │  │   degradation   │ │
│  │ • Security      │  │   monitoring    │  │ • Auto-recovery │ │
│  │   events        │  │ • User consent  │  │ • Backup &      │ │
│  │ • Threat        │  │ • Privacy       │  │   restore       │ │
│  │   intelligence  │  │   controls      │  │                 │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 10.6 Repository Structure Diagram

```
mecka/
│
├── README.md                          # Project overview
├── LICENSE                            # License file
├── CONTRIBUTING.md                    # Contribution guidelines
│
├── .github/
│   ├── workflows/
│   │   ├── ci.yml                     # Main CI pipeline
│   │   ├── security.yml               # Security checks
│   │   └── release.yml                # Release automation
│   ├── ISSUE_TEMPLATE/                # Bug/feature templates
│   └── PULL_REQUEST_TEMPLATE/         # PR templates
│
├── docs/
│   ├── architecture/                  # Architecture docs
│   ├── api/                          # API documentation
│   ├── deployment/                    # Deployment guides
│   └── tutorials/                     # How-to guides
│
├── examples/
│   ├── android/                      # Android examples
│   ├── ios/                          # iOS examples
│   ├── plugins/                      # Plugin examples
│   └── demos/                        # Demo applications
│
├── packages/
│   ├── core/                          # Shared libraries
│   │   ├── agent-runtime/             # Agent execution
│   │   ├── ipc/                      # Inter-process comm
│   │   ├── security/                 # Security primitives
│   │   └── common/                   # Common utilities
│   │
│   ├── platform/                      # Platform abstractions
│   │   ├── android/                  # Android impl
│   │   ├── ios/                      # iOS impl
│   │   ├── windows/                  # Windows impl
│   │   ├── macos/                    # macOS impl
│   │   └── linux/                    # Linux impl
│   │
│   ├── agents/                       # Agent implementations
│   │   ├── security/                 # Security agents
│   │   ├── executor/                 # Action agents
│   │   ├── listener/                # Monitoring agents
│   │   └── ui/                       # Interface agents
│   │
│   └── plugins/                      # Extension plugins
│
├── tools/
│   ├── build/                        # Build scripts
│   ├── deploy/                       # Deployment tools
│   ├── test/                         # Testing utilities
│   └── analysis/                     # Static analysis
│
└── android/                          # Android reference impl
    ├── app/                          # Main Android app
    ├── accessibility/                # Accessibility service
    ├── bluetooth/                    # Bluetooth integration
    ├── audio/                        # Audio processing
    └── notification/                # Notification listener
```

## Open Questions & Known Constraints

### Technical Questions

1. **Cross-Platform Audio Latency**: What are the acceptable latency thresholds for voice interaction across different platforms?

2. **Resource Constraints on iOS**: How do iOS background execution limits affect long-running agent operations?

3. **Permission Model Evolution**: How will the permission system adapt as platforms introduce new security models?

4. **Voiceprint Storage**: What are the regulatory requirements for voice biometric data storage across jurisdictions?

5. **Interoperability Standards**: Should Mecka adopt any existing agent communication standards (FIPA, etc.)?

### Known Constraints

1. **Platform Limitations**:
   - iOS: Limited accessibility API, no notification listener
   - Android: Strict background execution limits (Doze mode)
   - Windows: UWP sandboxing restrictions
   - macOS: Gatekeeper and notarization requirements

2. **Security Boundaries**:
   - Cannot bypass platform security models
   - Must respect user consent and privacy
   - No system-level root access without explicit user action

3. **Performance Considerations**:
   - Audio processing latency requirements
   - Real-time response constraints
   - Battery life impact on mobile devices

4. **Regulatory Compliance**:
   - GDPR requirements for data collection
   - Voice biometric regulations
   - Accessibility compliance requirements

### Design Decisions Requiring Validation

1. **Message Bus Architecture**: Is pub/sub the right pattern for all inter-agent communication?

2. **Arbitration Algorithm**: Will the priority-based conflict resolution scale to large agent sets?

3. **Security Model**: Is the offline-first approach secure enough for production use?

4. **Platform Abstraction**: Can the PAL design successfully hide platform differences while maintaining performance?

5. **Agent Lifecycle**: Are the proposed lifecycle states sufficient for all agent types?

## Conclusion

This specification provides the foundational architecture for Mecka Agentic Operating Intelligence, designed as a consent-based, security-first, cross-platform agent system. The Android implementation serves as the reference implementation to validate the architecture before porting to other platforms.

The specification emphasizes:
- **User consent and control** at every level
- **Security-first design** with robust isolation
- **Platform abstraction** for cross-platform compatibility
- **Scalable architecture** for complex agent interactions
- **Performance optimization** for real-time voice interaction

This document will serve as the canonical reference for Phase 1-4 implementation work, with regular updates as the architecture evolves through implementation experience and user feedback.