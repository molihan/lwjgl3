/* 
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package org.lwjgl.opengl.templates

import org.lwjgl.generator.*
import org.lwjgl.opengl.*
import org.lwjgl.system.windows.*

fun WGL_NV_gpu_affinity() = "WGLNVGpuAffinity".nativeClassWGL("WGL_NV_gpu_affinity", "NV") {
	nativeImport (
		"OpenGL.h",
	    "WGL.h"
	)

	javaDoc(
		"""
		Native bindings to the ${link("http://www.opengl.org/registry/specs/NV/gpu_affinity.txt", templateName)} extension.

		On systems with more than one GPU it is desirable to be able to select which GPU(s) in the system become the target for OpenGL rendering commands. This
		extension introduces the concept of a GPU affinity mask. OpenGL rendering commands are directed to the GPU(s) specified by the affinity mask. GPU
		affinity is immutable. Once set, it cannot be changed.

		This extension also introduces the concept called affinity-DC. An affinity-DC is a device context with a GPU affinity mask embedded in it. This
		restricts the device context to only allow OpenGL commands to be sent to the GPU(s) in the affinity mask.
		"""
	)

	val wglMakeCurrent = "{@link org.lwjgl.system.windows.WGL#wglMakeCurrent}"
	val wglMakeContextCurrentARB = "{@link WGLARBMakeCurrentRead#wglMakeContextCurrentARB}"

	IntConstant.block(
		"New error code set by wglShareLists, wglMakeCurrent and $wglMakeContextCurrentARB.",

		"ERROR_INCOMPATIBLE_AFFINITY_MASKS_NV" _ 0x20D0
	).noPrefix()

	IntConstant.block(
		"New error code set by $wglMakeCurrent and $wglMakeContextCurrentARB.",

		"ERROR_MISSING_AFFINITY_MASK_NV" _ 0x20D1
	)

	// Type definitions

	val HGPUNV = PointerType(name = "HGPUNV", includesPointer = true)
	val HGPUNV_p = PointerType(name = "HGPUNV", mapping = PointerMapping.DATA_POINTER)

	val PGPU_DEVICE = StructType(
		name = "PGPU_DEVICE",
		includesPointer = true,
		definition = struct("org.lwjgl.opengl", "GPU_DEVICE", "wgl") {
			javaDoc(
				"""
				Receives information about the display device specified by the {@code deviceIndex} parameter of the {@link WGLNVGpuAffinity#wglEnumGpuDevicesNV}
				function.
				"""
			)
			nativeImport("WindowsLWJGL.h", "WGL.h")
			DWORD.member("cb")
			TCHAR.member(nativeName = "DeviceName", size = 32, nullTerminated = true)
			TCHAR.member(nativeName = "DeviceString", size = 128, nullTerminated = true)
			DWORD.member("Flags")
			RECT.member(nativeName = "rcVirtualScreen", name = "virtualScreen");
		}
	)

	// Functions

	BOOL.func(
		"EnumGpusNV",
		"""
		Returns the handles for all GPUs in a system.

		By looping over {@code wglEnumGpusNV} and incrementing {@code gpuIndex}, starting at index 0, all GPU handles can be queried. If the function succeeds,
		the return value is TRUE. If the function fails, the return value is FALSE and {@code gpu} will be unmodified. The function fails if {@code gpuIndex} is
		greater or equal than the number of GPUs supported by the system.
		""",

		UINT.IN("gpuIndex", "an index value that specifies a GPU"),
		HGPUNV_p.IN("gpu", "returns a handle for GPU number {@code gpuIndex}. The first GPU will be index 0.")
	)

	BOOL.func(
		"EnumGpuDevicesNV",
	    "Retrieve information about the display devices supported by a GPU.",

	    HGPUNV.IN("gpu", "a handle to the GPU to query"),
		UINT.IN("deviceIndex", "an index value that specifies a display device, supported by {@code gpu}, to query. The first display device will be index 0."),
		PGPU_DEVICE.IN("gpuDevice", "a {@link GPU_DEVICE} structure which will receive information about the display device at index {@code deviceIndex}.")
	)

	HDC.func(
		"CreateAffinityDCNV",
		"""
		Creates an affinity-DC. Affinity-DCs, a new type of DC, can be used to direct OpenGL commands to a specific GPU or set of GPUs. An affinity-DC is a
		device context with a GPU affinity mask embedded in it. This restricts the device context to only allow OpenGL commands to be sent to the GPU(s) in the
		affinity mask. An affinity-DC can be created directly, using the new function {@code wglCreateAffinityDCNV} and also indirectly by calling
		{@link WGLARBPbuffer#wglCreatePbufferARB} followed by {@link WGLARBPbuffer#wglGetPbufferDCARB}.

		If successful, the function returns an affinity-DC handle. If it fails, NULL will be returned.
		""",

		mods(const, nullTerminated) _ HGPUNV_p.IN("gpuList", "a NULL-terminated array of GPU handles to which the affinity-DC will be restricted")
	)

	BOOL.func(
		"EnumGpusFromAffinityDCNV",
		"""
		Retrieves a list of GPU handles that make up the affinity-mask of an affinity-DC.

		By looping over {@code wglEnumGpusFromAffinityDCNV} and incrementing {@code gpuIndex}, starting at index 0, all GPU handles associated with the DC can
		be queried. If the function succeeds, the return value is TRUE. If the function fails, the return value is FALSE and {@code gpu} will be unmodified. The
		function fails if {@code gpuIndex} is greater or equal than the number of GPUs associated with {@code affinityDC}.
		""",

		HDC.IN("affinityDC", "a handle of the affinity-DC to query"),
		UINT.IN("gpuIndex", "an index value of the GPU handle in the affinity mask of {@code affinityDC} to query"),
		HGPUNV_p.IN("gpu", "returns a handle for  GPU number {@code gpuIndex}. The first GPU will be at index 0.")
	)

	BOOL.func(
		"DeleteDCNV",
		"Deletes an affinity-DC.",

		HDC.IN("hdc", "a handle of an affinity-DC to delete")
	)
}